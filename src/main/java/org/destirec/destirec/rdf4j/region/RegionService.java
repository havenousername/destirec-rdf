package org.destirec.destirec.rdf4j.region;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.destirec.destirec.rdf4j.attribute.QualityOntology;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.region.apiDto.ExternalRegionDto;
import org.destirec.destirec.rdf4j.region.apiDto.SimpleRegionDto;
import org.destirec.destirec.rdf4j.region.cost.CostDao;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.eclipse.rdf4j.model.IRI;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;


@Service
public class RegionService {
    private final RegionDao regionDao;

    protected Logger logger = LoggerFactory.getLogger(getClass());
    private final QualityOntology qualityOntology;

    private final DestiRecOntology destiRecOntology;

    private final CostDao costDao;

    private final ScheduledExecutorService scheduledService;

    private final Retry dbRetry;


    public RegionService(RegionDao regionDao, DestiRecOntology ontology, CostDao costDao) {
        this.regionDao = regionDao;

        destiRecOntology = ontology;
        qualityOntology = new QualityOntology(
                destiRecOntology,
                destiRecOntology.getFactory(),
                regionDao.getRdf4JTemplate()
        );
        this.costDao = costDao;

        scheduledService = Executors.newSingleThreadScheduledExecutor();

        RetryConfig dbRetryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryOnException(e -> e instanceof Exception && !(e instanceof IllegalArgumentException))
                .build();
        this.dbRetry = Retry.of("rdf-operations", dbRetryConfig);
    }

    private <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        return Retry.decorateSupplier(dbRetry, () -> {
            try {
                return operation.get();
            } catch (Exception e) {
                logger.error("Failed to execute {}", operationName, e);
                throw new RuntimeException("Failed to execute " + operationName, e);
            }
        }).get();
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
                (entry) -> regionDao.getMonthDao().getDtoCreator().create(Map.entry(entry.getKey(), new Pair<>(entry.getValue(), true)))
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
    public List<RegionDto> getRegions(int page, int size, String sortField, String sortOrder) {
        return regionDao.listPaginated(page, size);
    }

    @Transactional
    public List<CostDto> getCosts() {
        return costDao.list();
    }

    @Transactional
    public List<RegionDto> getLeafRegions() {
        return regionDao.listLeaf();
    }


    private void validateDto(SimpleRegionDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Region DTO cannot be null");
        }
    }

    private Optional<IRI> getParentRegion(SimpleRegionDto dto) {
        if (dto.getSourceParent() == null) {
            return Optional.empty();
        }
        return Optional.of(regionDao.getBySource(dto.getSourceParent()));
    }

    private RegionDto createNewRegionWithParent(SimpleRegionDto dto, Optional<IRI> parent) {
        IRI parentId = parent.orElse(null);
        return regionDao.getDtoCreator().create(dto, parentId);
    }

    @Transactional
    public IRI createRegion(SimpleRegionDto dto, boolean isBulky) {
        validateDto(dto);
        Optional<IRI> parentRegion = getParentRegion(dto);
        RegionDto newRegion = createNewRegionWithParent(dto, parentRegion);

        logger.info("Create region with DTO: {}", newRegion);
        IRI regionId = executeWithRetry(() -> regionDao.saveAndReturnId(newRegion), "create region");
        if (!isBulky) {
            updateParentChildOntologiesAsync(newRegion);
        }
        logger.info("Region with DTO " + regionId + " was created");
        return regionId;
    }

    private void updateAllOntologies() {
        long totalNumberOfRegions = regionDao.getTotalCount();
        int perPage = 250;
        int currentPage = 0;
        int numberOfPages = (int) Math.ceil(totalNumberOfRegions / (double) perPage);
        logger.info("Update ontologies for {} regions", totalNumberOfRegions);
        while (currentPage < numberOfPages) {
            List<RegionDto> regions = regionDao.listPaginated(currentPage, perPage);
            qualityOntology.defineRegionsQualities(regions, ""+currentPage);
            destiRecOntology.migrate(""+currentPage);
            destiRecOntology.triggerInference();
            currentPage++;
        }
    }

    private void updateParentChildOntologies(RegionDto child) {
        if (child.getParentRegion() == null) {
            qualityOntology.defineRegionsQualities(List.of(child), ""+child.id());
            destiRecOntology.migrate(""+child.id());
            destiRecOntology.triggerInference();
            return;
        }
        RegionDto parent = regionDao.getById(child.getParentRegion());
        qualityOntology.defineRegionsQualities(List.of(child, parent), ""+child.id());
        destiRecOntology.migrate(""+child.id());
        destiRecOntology.triggerInference();
    }

    public void updateAllOntologiesAsync() {
        scheduledService.schedule(this::updateAllOntologies, 100, TimeUnit.MILLISECONDS);
    }

    public void updateParentChildOntologiesAsync(RegionDto child) {
        scheduledService.schedule(() -> updateParentChildOntologiesAsync(child), 100, TimeUnit.MILLISECONDS);
    }

    @Transactional
    public IRI createRegion(ExternalRegionDto regionDto) {
        if (regionDto.graphId() != null) {
            String msg = "Region with ID " + regionDto.id() + " is already present in the RDF database";
            throw new IllegalArgumentException(msg);
        }

        return executeWithRetry(() -> {
            List<MonthDto> monthDtos = new ArrayList<>();
            List<FeatureDto> featureDtos = new ArrayList<>();
            CostDto costDto;
            var cost = regionDto.getCost();
            CostDto cost1 = regionDao.getCostDao()
                    .getDtoCreator().create(cost.get(0), cost.get(1));
            costDto = regionDao.getCostDao()
                    .save(cost1);

            if (!regionDto.getMonths().isEmpty()) {
                monthDtos = regionDto
                        .getMonths()
                        .entrySet()
                        .stream()
                        .map(month -> regionDao.getMonthDao().getDtoCreator()
                                .create(Map.entry(month.getKey(), new Pair<>(month.getValue(), true))))
                        .map(dto -> regionDao.getMonthDao().save(dto))
                        .toList();
            }

            if (!regionDto.getFeatures().isEmpty()) {
                featureDtos  = regionDto
                        .getFeatures()
                        .entrySet()
                        .stream()
                        .map(feature -> regionDao.getFeatureDao().getDtoCreator().createFromEnum(feature))
                        .map(dto -> regionDao.getFeatureDao().save(dto))
                        .toList();
            }

            RegionDto regionDtoForCreate = regionDao.getDtoCreator()
                    .create(regionDto, featureDtos, monthDtos, costDto);

            logger.info("Create region with DTO " + regionDtoForCreate);
            RegionDto readyRegionDto = regionDao.save(regionDtoForCreate);

            updateParentChildOntologiesAsync(readyRegionDto);
            logger.info("Region with DTO " + readyRegionDto + " was created");
            return readyRegionDto.id();
        }, "create region with dependencies");

    }

    public String getRegionSelect() {
        return regionDao.getReadQuery();
    }
}
