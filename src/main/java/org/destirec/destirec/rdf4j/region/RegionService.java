package org.destirec.destirec.rdf4j.region;

import org.destirec.destirec.rdf4j.attribute.QualityOntology;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.ontology.OntologyFeature;
import org.destirec.destirec.rdf4j.region.apiDto.ExternalRegionDto;
import org.destirec.destirec.rdf4j.region.cost.CostDao;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


@Service
public class RegionService {
    private final RegionDao regionDao;

    protected Logger logger = LoggerFactory.getLogger(getClass());
    private final QualityOntology qualityOntology;

    private final DestiRecOntology destiRecOntology;

    private final CostDao costDao;


    public RegionService(RegionDao regionDao, DestiRecOntology ontology, CostDao costDao) {
        this.regionDao = regionDao;

        destiRecOntology = ontology;
        qualityOntology = new QualityOntology(
                destiRecOntology,
                destiRecOntology.getFactory(),
                regionDao
        );
        this.costDao = costDao;
    }

    private <DtoT extends Dto, MapKey, MapValue> void updateListEntities(
            String entityName,
            Map<MapKey, MapValue> incomingEntities,
            List<DtoT> existingEntities,
            Function<Map.Entry<DtoT, MapKey>, Boolean> filterEntity,
            GenericDao<?, DtoT> dao,
            Function<Map.Entry<MapKey, MapValue>, DtoT> creator
    ) {
        logger.info("Update " + entityName + " if needed");
        List<IRI> updatedEntities = new ArrayList<>();
        incomingEntities.forEach((key, value) -> {
            var currentEntity = existingEntities
                    .stream()
                    .filter((e) -> filterEntity.apply(Map.entry(e, key)))
                    .findAny();
            DtoT newEntity = creator.apply(Map.entry(key, value));
            boolean presentUpdate = currentEntity.isPresent() && !currentEntity.get().equals(newEntity);
            boolean emptyUpdate = currentEntity.isEmpty();
            if (presentUpdate) {
                IRI newMonthIRI = dao.saveAndReturnId(newEntity, currentEntity.get().id());
                updatedEntities.add(newMonthIRI);
            } else if (emptyUpdate) {
                IRI newMonthIRI = dao.saveAndReturnId(newEntity);
                updatedEntities.add(newMonthIRI);
            }
        });
        logger.info("Updated " + entityName + " are " + updatedEntities);
    }

    private void updateMonths(ExternalRegionDto regionDto, List<MonthDto> existingMonths) {
//        logger.info("Update regions months if needed");
//        List<IRI> updatedMonths = new ArrayList<>();
//        regionDto.getMonths().forEach((month, value) -> {
//            var currentMonth = existingMonths
//                    .stream()
//                    .filter(m -> m.month().equals(month))
//                    .findAny();
//            var newMonth = regionDao
//                    .getMonthDao()
//                    .getDtoCreator()
//                    .create(month, value);
//            boolean presentUpdate = currentMonth.isPresent() && !currentMonth.get().equals(newMonth);
//            boolean emptyUpdate = currentMonth.isEmpty();
//            if (presentUpdate) {
//                IRI newMonthIRI = regionDao.getMonthDao().saveAndReturnId(newMonth, currentMonth.get().id());
//                updatedMonths.add(newMonthIRI);
//            } else if (emptyUpdate) {
//                IRI newMonthIRI = regionDao.getMonthDao().saveAndReturnId(newMonth);
//                updatedMonths.add(newMonthIRI);
//            }
//        });
//        logger.info("Updated months are " + updatedMonths);

        updateListEntities(
                "months",
                regionDto.getMonths(),
                existingMonths,
                (entry) -> entry.getKey().getMonth().equals(entry.getValue()),
                regionDao.getMonthDao(),
                (entry) -> regionDao.getMonthDao().getDtoCreator().create(entry)
        );
    }

    private void updateFeatures(ExternalRegionDto regionDto, List<FeatureDto> existingFeatures) {
        updateListEntities(
                "features",
                regionDto.getFeatures(),
                existingFeatures,
                (entry) -> entry.getKey().getRegionFeature().name().equals(entry.getValue().name()),
                regionDao.getFeatureDao(),
                (entry) -> regionDao.getFeatureDao().getDtoCreator().createFromEnum(entry)
        );
    }

    private void updateCost(CostDto currentCost, List<Integer> newCost) {
        logger.info("Update cost if needed");
        var changedWeeklyCost = currentCost.getCostPerWeek() != newCost.getFirst();
        var changedBudgetLevel = currentCost.getBudgetLevel() != newCost.getLast();
        if (changedWeeklyCost || changedBudgetLevel) {
            CostDto newCostDto = regionDao.getCostDao().getDtoCreator()
                    .create(newCost.getFirst(), newCost.getLast());
            IRI costIri = regionDao.getCostDao().saveAndReturnId(newCostDto, currentCost.id());
            logger.info("Updated cost is " + costIri);
        }
    }

    @Transactional
    public IRI updateRegion(ExternalRegionDto regionDto) {
        regionDao.getConfigFields()
                .setFeatureNames(null);
        Optional<RegionDto> existingRegion = regionDao.getByIdOptional(regionDao.getDtoCreator().createId(regionDto.id()));
        if (existingRegion.isEmpty()) {
            throw new IllegalArgumentException("Cannot find region for updating it");
        }

        updateMonths(regionDto, existingRegion.get().getMonths());
        updateFeatures(regionDto, existingRegion.get().getFeatures());
        updateCost(existingRegion.get().getCost(), regionDto.getCost());

        RegionDto regionDtoForCreate = regionDao.getDtoCreator()
                .create(
                        regionDto,
                        existingRegion.get().getFeatures(),
                        existingRegion.get().getMonths(),
                        existingRegion.get().getCost());
        logger.info("Updated region with DTO " + regionDtoForCreate);
        IRI regionId = regionDao.saveAndReturnId(regionDtoForCreate);
        logger.info("Region with DTO" + regionId + " was updated");
        return regionId;
    }

    @Transactional
    public Optional<RegionDto> getRegion(String id) {
        return regionDao.getByIdOptional(regionDao.getDtoCreator().createId(id));
    }

    @Transactional
    public List<RegionDto> getRegions() {
        return regionDao.list();
    }

    @Transactional
    public List<CostDto> getCosts() {
        return costDao.list();
    }

    @Transactional
    public List<RegionDto> getLeafRegions() {
        return regionDao.listLeaf();
    }

    @Transactional
    public IRI createRegion(ExternalRegionDto regionDto) {
        if (regionDto.graphId() != null) {
            String msg = "Region with ID " + regionDto.id() + " is already present in the RDF database";
            throw new IllegalArgumentException(msg);
        }
        var cost = regionDto.getCost();
        CostDto cost1 = regionDao.getCostDao()
                .getDtoCreator().create(cost.get(0), cost.get(1));
        CostDto costDto = regionDao.getCostDao()
                .save(cost1);


        List<MonthDto> monthDtos = regionDto
                .getMonths()
                .entrySet()
                .stream()
                .map(month -> regionDao.getMonthDao().getDtoCreator().create(month))
                .map(dto -> regionDao.getMonthDao().save(dto))
                .toList();

        List<FeatureDto> featureDtos = regionDto
                .getFeatures()
                .entrySet()
                .stream()
                .map(feature -> regionDao.getFeatureDao().getDtoCreator().createFromEnum(feature))
                .map(dto -> regionDao.getFeatureDao().save(dto))
                .toList();

        RegionDto regionDtoForCreate = regionDao.getDtoCreator()
                .create(regionDto, featureDtos, monthDtos, costDto);

        logger.info("Create region with DTO " + regionDtoForCreate);
        IRI regionId = regionDao.saveAndReturnId(regionDtoForCreate);

        qualityOntology.defineRegionsQualities(regionDao.listLeaf(), OntologyFeature.REGION_QUALITY);
        destiRecOntology.migrate(OntologyFeature.REGION_QUALITY);
        destiRecOntology.resetOntologyFeature(OntologyFeature.REGION_QUALITY);
        destiRecOntology.triggerInference();
        logger.info("Region with DTO " + regionId + " was created");
        return regionId;
    }

    public String getRegionSelect() {
        return regionDao.getReadQuery();
    }
}
