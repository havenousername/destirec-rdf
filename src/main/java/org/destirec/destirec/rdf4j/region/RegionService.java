package org.destirec.destirec.rdf4j.region;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.destirec.destirec.rdf4j.attribute.QualityOntology;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.knowledgeGraph.POIClass;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.poi.POIDao;
import org.destirec.destirec.rdf4j.poi.POIDto;
import org.destirec.destirec.rdf4j.region.apiDto.ExternalRegionDto;
import org.destirec.destirec.rdf4j.region.apiDto.SimpleRegionDto;
import org.destirec.destirec.rdf4j.region.cost.CostDao;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames.Individuals.RegionFeature;
import org.destirec.destirec.utils.rdfDictionary.RegionNames.Individuals.RegionTypes;
import org.eclipse.rdf4j.model.IRI;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
public class RegionService {
    private final RegionDao regionDao;

    protected Logger logger = LoggerFactory.getLogger(getClass());
    private final QualityOntology qualityOntology;

    private final DestiRecOntology destiRecOntology;

    private final CostDao costDao;

    private final ScheduledExecutorService scheduledService;

    private final Retry dbRetry;

    private final POIDao poiDao;


    public RegionService(RegionDao regionDao, DestiRecOntology ontology, CostDao costDao, POIDao poiDao) {
        this.regionDao = regionDao;

        destiRecOntology = ontology;
        qualityOntology = new QualityOntology(
                destiRecOntology,
                destiRecOntology.getFactory(),
                regionDao.getRdf4JTemplate()
        );
        this.costDao = costDao;
        this.poiDao = poiDao;

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
        updateCost(Optional.of(existingRegion.get().getCost()).orElse(
                regionDao.getCostDao().getDtoCreator().create(0, 0)), regionDto.getCost());

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
        try {
            return Optional.ofNullable(regionDao.getByIdSafe(regionDao.getDtoCreator().createId(id)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<RegionDto> getRegion(IRI id) {
        return regionDao.getByIdOptional(id);
    }

//    @Transactional
//    public List<RegionDto> getRegionsLevelFromChild(RegionDto child, RegionNames level) {
//        regionDao.getListAllByTypeQueryId()
//    }

    @Transactional
    public Optional<POIDto> getPOI(IRI id) {
        return poiDao.getByIdOptional(id);
    }

    @Transactional
    public Optional<POIDto> getPOI(String id) {
        return poiDao.getByIdOptional(poiDao.getDtoCreator().createId(id));
    }

    @Transactional
    public POIDto getPOISafe(String id) {
        return poiDao.getById(poiDao.getDtoCreator().createId(id));
    }

    @Transactional
    public List<RegionDto> getRegions(int page, int size, String sortField, String sortOrder) {
        return regionDao.listPaginated(page, size);
    }


    @Transactional
    public List<RegionDto> getRegionsByType(RegionTypes type, int page, int size) {
        return regionDao.listAllByType(type, page, size);
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
    public List<IRI> createPOIs(List<POIClass> poiClasses) {
        List<IRI> parents = poiClasses.stream().map(i -> regionDao.getBySource(i.getSourceParent())).toList();
        if (parents.size() != poiClasses.size()) {
            throw new IllegalArgumentException("It is a requirement that the parent exists for POI");
        }

        List<FeatureDto> featuresDtoRuntime = poiClasses.stream()
                .map(i -> poiDao
                        .getFeatureDao()
                        .getDtoCreator()
                        .createFromEnum(Map.entry(i.getFeature(), i.getPercentageScore())))
                .toList();

        List<FeatureDto> savedFeatures = poiDao.getFeatureDao().bulkSaveListGet(featuresDtoRuntime);
        assert savedFeatures.size() == featuresDtoRuntime.size() && featuresDtoRuntime.size() == poiClasses.size();
        List<POIDto> poiRuntime = IntStream.range(0, savedFeatures.size())
                .mapToObj(i -> poiDao.getDtoCreator().create(poiClasses.get(i), savedFeatures.get(i), parents.get(i)))
                .toList();
        List<IRI> savedPois = poiDao.bulkSaveList(poiRuntime);
        assert savedPois.size() == poiRuntime.size() && poiRuntime.size() == poiClasses.size();
        return savedPois;
    }

    @Transactional
    public IRI createPOI(POIClass poiClass) {
        if (poiClass.getSourceParent() == null) {
            throw new IllegalArgumentException("POI must have a parent. POI class " + poiClass);
        }
        IRI parentRegion = regionDao.getBySource(poiClass.getSourceParent());

        if (parentRegion == null) {
            throw new IllegalArgumentException("It is a requirement that the parent exists for POI");
        }

        FeatureDto featureDtoRuntime = poiDao
                .getFeatureDao()
                .getDtoCreator()
                .createFromEnum(Map.entry(poiClass.getFeature(), poiClass.getPercentageScore()));

        FeatureDto savedFeature = poiDao.getFeatureDao()
                .save(featureDtoRuntime);

        POIDto poiDto = poiDao.getDtoCreator()
                .create(poiClass, savedFeature, parentRegion);

        return poiDao.saveAndReturnId(poiDto);
    }

    @Transactional
    public void createRegion(SimpleRegionDto dto, boolean isBulky) {
        validateDto(dto);
        Optional<IRI> parentRegion = getParentRegion(dto);
        RegionDto newRegion = createNewRegionWithParent(dto, parentRegion);

        logger.info("Create region with DTO: {}", newRegion);
        IRI regionId = executeWithRetry(() -> regionDao.saveAndReturnId(newRegion), "create region");
        if (!isBulky) {
            updateParentChildOntologiesAsync(newRegion);
        }
        logger.info("Region with DTO " + regionId + " was created");
    }

    public long getTotalRegionsByType(RegionTypes type) {
        return regionDao.getTotalCountByType(type);
    }

    public long getTotalRegions() {
        return regionDao.getTotalCount();
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

    public void normalizeFeatureScores() {
        Map<RegionFeature, Long> maxScoresPerFeature = Arrays.stream(RegionFeature.values())
                .collect(Collectors.toMap(
                        feature -> feature,
                        poiDao::getMaxScoreForFeature
                ));

        long featureCount = poiDao.getFeatureDao().getTotalCount();
        int pageSize = 5000;
        for (int i = 0; i < Math.ceil((double) featureCount / pageSize) ; i++) {
            List<FeatureDto> features = poiDao.getFeatureDao().listPaginated(i, pageSize);
            for (var feature : features) {
                var maxScore = maxScoresPerFeature.get(feature.getRegionFeature());
                if (maxScore == null || maxScore == 0) {
                    continue;
                }
                feature.setHasScore((int) ((feature.getHasScore() * 100) / maxScore));

            }
            poiDao.getFeatureDao().bulkSaveList(features);
            logger.info("Normalized scores for features");
        }
    }

    public void updateAllOntologiesPOIs() {
        long totalNumberOfPois = poiDao.getTotalCount();
        int perPage = 250;
        int currentPage = 0;
        int numberOfPages = (int) Math.ceil(totalNumberOfPois / (double) perPage);
        logger.info("Update ontologies for {} pois", totalNumberOfPois);
        while (currentPage < numberOfPages) {
            List<POIDto> pois = poiDao.listPaginated(currentPage, perPage);
            qualityOntology.definePOIOntology(pois, ""+currentPage);
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
        scheduledService.schedule(() -> {

        }, 100, TimeUnit.MILLISECONDS);
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
