package org.destirec.destirec.rdf4j.knowledgeGraph;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.constraints.Null;
import org.destirec.destirec.rdf4j.interfaces.Rdf4jTemplate;
import org.destirec.destirec.rdf4j.overpass.OverpassService;
import org.destirec.destirec.rdf4j.region.RegionDao;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.region.RegionService;
import org.destirec.destirec.rdf4j.region.apiDto.SimpleRegionDto;
import org.destirec.destirec.rdf4j.version.VersionDao;
import org.destirec.destirec.rdf4j.vocabulary.DBPEDIA;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA;
import org.destirec.destirec.utils.TupleContainer;
import org.destirec.destirec.utils.URIHandling;
import org.destirec.destirec.utils.rdfDictionary.RegionNames.Individuals.RegionTypes;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.impl.MutableTupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KnowledgeGraphService {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected final RDF4JTemplate rdf4JTemplate;
    protected final Rdf4jTemplate threadSafeRdf4JTemplate;
    protected final RegionService regionService;
    private final Map<RegionTypes, Function<TupleContainer<String>, String>> queries;
    private final SimpleValueFactory factory = SimpleValueFactory.getInstance();
    private final VersionDao versionDao;
    private final RateLimiter rateLimiter;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(2);
    private static final int MAX_REQUESTS_PER_SECOND = 40;
    private static final int WIKIDATA_BATCH_SIZE = 10;
    private static final int DBPEDIA_BATCH_SIZE = 10;
    private final CircuitBreaker circuitBreaker = CircuitBreaker.of("wikidata-api",
            CircuitBreakerConfig.custom()
                    .failureRateThreshold(50)
                    .waitDurationInOpenState(Duration.ofMinutes(1))
                    .build());
    private final Repository repository;

    private final OverpassService overpassService;

    @Lazy
    @Autowired
    private KnowledgeGraphService self;
    @Autowired
    private RegionDao regionDao;

    @org.springframework.beans.factory.annotation.Value("${app.env.kg.is_test_run}")
    private boolean isTestRun;

    @org.springframework.beans.factory.annotation.Value("${app.env.kg.max_query_number}")
    private int queryNumberLimit;

    @org.springframework.beans.factory.annotation.Value("${app.env.kg.regions_version}")
    private int regionVersion;

    @org.springframework.beans.factory.annotation.Value("${app.env.kg.poi_version}")
    private int poiVersion;

    private final MeterRegistry meterRegistry;
    private final Counter regionsProcessed;
    private final Timer processingTime;


    public KnowledgeGraphService(
            RDF4JTemplate rdf4JTemplate,
            Rdf4jTemplate threadSafeRdf4JTemplate,
            RegionService regionService,
            VersionDao versionDao,
            Repository repository,
            OverpassService overpassService,
            MeterRegistry meterRegistry
    ) {
        this.rdf4JTemplate = rdf4JTemplate;
        this.threadSafeRdf4JTemplate = threadSafeRdf4JTemplate;
        this.regionService = regionService;
        this.versionDao = versionDao;
        this.repository = repository;
        this.overpassService = overpassService;
        queries = new LinkedHashMap<>();

        queries.put(RegionTypes.WORLD, this::buildWorldQueryString);
        queries.put(RegionTypes.CONTINENT, this::buildContinentQueryString);
//        queries.put(RegionTypes.CONTINENT_REGION, this::buildContinentRegionQueryString);
        queries.put(RegionTypes.COUNTRY, this::buildCountryQueryString);
        queries.put(RegionTypes.DISTRICT, this::getDistrictsCountries);

        // initialize metrics
        this.meterRegistry = meterRegistry;
//        queries.put(RegionTypes.POI, this::getPOIs);

        regionsProcessed = Counter.builder("regions_processed")
                .description("Number of regions processed")
                .register(meterRegistry);

        processingTime = Timer.builder("region_processing_time")
                .description("Time taken to process regions")
                .register(meterRegistry);

        rateLimiter = RateLimiter.create(MAX_REQUESTS_PER_SECOND);
    }

    private String buildPOIDBPediaQueryString(List<String> iri) {
        return """
                PREFIX owl: <http://www.w3.org/2002/07/owl#>
                PREFIX bd: <http://www.bigdata.com/rdf#>
                PREFIX wikibase: <http://wikiba.se/ontology#>
                PREFIX dbo: <http://dbpedia.org/ontology/>
                PREFIX dct: <http://purl.org/dc/terms/>
                PREFIX foaf: <http://xmlns.com/foaf/0.1/>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                 # DBpedia enrichment using owl:sameAs
                 SELECT ?outDegree ?dbpedia ?thumb WHERE {
                    SERVICE <https://dbpedia.org/sparql> {
                      SELECT ?outDegree ?dbpedia ?thumb {
                        VALUES ?wikidataPoiUri {  %s }
                        ?dbpedia owl:sameAs ?wikidataPoiUri .
                        OPTIONAL { ?dbpedia dbo:wikiPageOutDegree ?outDegree }
                        OPTIONAL { ?dbpedia dbo:thumbnail ?thumb }
                      }
                  }
                 }
                """.formatted(iri.stream().map(uri -> "<" + uri + ">")
                .collect(Collectors.joining(" ")));
    }

    private String buildPoiQueryString(List<String> districts) {
        String districtValues = districts.stream().map(uri -> "<" + uri + ">").collect(Collectors.joining(" "));
        return """
                        PREFIX owl: <http://www.w3.org/2002/07/owl#>
                        PREFIX bd: <http://www.bigdata.com/rdf#>
                        PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                        PREFIX wd: <http://www.wikidata.org/entity/>
                        PREFIX wikibase: <http://wikiba.se/ontology#>
                        PREFIX dbo: <http://dbpedia.org/ontology/>
                        PREFIX dct: <http://purl.org/dc/terms/>
                        PREFIX foaf: <http://xmlns.com/foaf/0.1/>
                        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                
                        SELECT DISTINCT ?poi ?poiLabel ?officialWebsite ?image ?coord
                         ?osmId ?statementCount ?siteLinks ?dbpedia ?district
                         ?quoraTopicID ?tripAdvisorID ?twitterUsername ?imdbKeywordID ?type
                        WHERE {
                          SERVICE <https://query.wikidata.org/sparql> {
                            SELECT ?poi ?poiLabel ?officialWebsite ?image ?coord
                            ?osmId ?statementCount ?siteLinks ?dbpedia ?district
                            ?quoraTopicID ?tripAdvisorID ?twitterUsername ?imdbKeywordID ?type
                            WHERE {
                              VALUES ?district { %s }
                              VALUES ?type {
                                wd:Q8502    # MOUNTAIN
                                wd:Q23397   # LAKE
                                wd:Q22698   # PARK
                                wd:Q5469146    # FOREST
                                wd:Q150784  # CANYON
                                wd:Q15243209  # HISTORIC_DISTRICT
                                wd:Q9259 # UNESCO SITE
                                wd:Q2977   # CATHEDRAL
                                wd:Q23413  # CASTLE
                                wd:Q2143825  # HIKING_TRAIL
                                wd:Q6017969 # LOOKOUT POINT
                                wd:Q1640361    # CLIMBING AREA
                                wd:Q1699583 # CLIMBING_ROUTE
                                wd:Q1109069 # SKI JUMPING HILL
                                wd:Q130003  # SKI_RESORT
                                wd:Q3141488 # SNOW_PARK
                                wd:Q179643 # DIVING_SPOT
                                wd:Q2368508  # SURF_SPOT
                                wd:Q740326   # WATER_PARK
                                wd:Q194195  # AMUSEMENT_PARK
                                wd:Q2416723 # THEME_PARK
                                wd:Q477396 # CIRCUS
                                wd:Q1232319 # KARTING_CIRCUIT
                                wd:Q521839 # SHOOTING_RANGE
                                wd:Q1777138 # RACE TRACK
                                wd:Q483110  # STADIUM
                                wd:Q1443808 # OCEANARIUM
                                wd:Q1265865 # ROLLER COASTER
                                wd:Q15060435 # MARINE MAMMAL PARK
                                wd:Q33097655  # ARCADE
                                wd:Q17015069 # ESCAPE_ROOM
                                wd:Q132241  # FESTIVAL
                                wd:Q183424  # FESTIVAL_VENUE
                                wd:Q1311064 # ICE_CREAM_SHOP
                                wd:Q18326400 # FLYING THEATER
                                wd:Q202570 # FERRIS WHEEL
                                wd:Q27106471 # BOWLING_ALLEY
                                wd:Q857909 # BEER_GARDEN
                                wd:Q11707   # RESTAURANT
                                wd:Q1316209 # STREET_FOOD_VENUE
                                wd:Q1192284  # FOOD_MARKET
                                wd:Q31374404   # SHOPPING_MALL
                                wd:Q865693 # SOUVENIR_SHOP
                                wd:Q3486441  # SKYCOASTER
                                wd:Q6882870 # SPA TOWN
                                wd:Q330284 # MARKET
                                wd:Q40080   # BEACH
                                wd:Q33506   # MUSEUM
                                wd:Q1007870  # ART GALLERY
                                wd:Q153562  # OPERA HOUSE
                                wd:Q24354   # THEATRE BUILDING
                              }
                
                              ?poi wdt:P31 ?type ;
                                   wdt:P131 ?district .  # Located in district
                
                              # Wikidata enrichments
                              OPTIONAL { ?poi wdt:P856 ?officialWebsite }
                              OPTIONAL { ?poi wdt:P18 ?image }
                              OPTIONAL { ?poi wdt:P625 ?coord }
                              OPTIONAL { ?poi wdt:P402 ?osmId }
                              OPTIONAL { ?poi wikibase:statements ?statementCount. }
                
                              # Popularity indicators
                              OPTIONAL { ?poi wdt:P3417 ?quoraTopicID }        # Quora
                              OPTIONAL { ?poi wdt:P3134 ?tripAdvisorID }       # TripAdvisor
                              OPTIONAL { ?poi wdt:P2002 ?twitterUsername }      # Twitter
                              OPTIONAL { ?poi wdt:P5021 ?imdbKeywordID }        # IMDb keyword
                
                              SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en". }
                            }
                          }
                        }
                """.formatted(districtValues);
    }

    private String buildWorldQueryString(@Null TupleContainer<String> parents) {
        return """
                PREFIX bd: <http://www.bigdata.com/rdf#>
                PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                PREFIX wd: <http://www.wikidata.org/entity/>
                PREFIX wikibase: <http://wikiba.se/ontology#>
                
                SELECT ?earth ?earthLabel {
                    SERVICE <https://query.wikidata.org/sparql> {
                        SELECT ?earth ?earthLabel WHERE {
                                    BIND(wd:Q2 AS ?earth)
                                    SERVICE wikibase:label {
                                bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en" .
                            }
                        }
                    }
                }
                """;
    }

    private String buildContinentQueryString(@Nullable TupleContainer<String> noName) {
        return """
                    PREFIX bd: <http://www.bigdata.com/rdf#>
                    PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                    PREFIX wd: <http://www.wikidata.org/entity/>
                    PREFIX wikibase: <http://wikiba.se/ontology#>
                
                    SELECT ?continent ?continentLabel ?geoShape {
                        SERVICE <https://query.wikidata.org/sparql> {
                            SELECT ?continent ?continentLabel ?geoShape WHERE {
                                VALUES ?continent {
                                    wd:Q15      # Africa
                                    wd:Q51      # Antarctica
                                    wd:Q48      # Asia
                                    wd:Q46      # Europe
                                    wd:Q49      # North America
                                    wd:Q18      # South America
                                    wd:Q538     # Australia
                                }
                
                                OPTIONAL { ?continent wdt:P3896 ?geoShape }
                
                                SERVICE wikibase:label {
                                    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en" .
                                }
                            }
                        }
                    }
                """;
    }

    private String buildContinentRegionQueryString(TupleContainer<String> continent) {
        return """
                    PREFIX bd: <http://www.bigdata.com/rdf#>
                    PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                    PREFIX wd: <http://www.wikidata.org/entity/>
                    PREFIX wikibase: <http://wikiba.se/ontology#>
                
                    SELECT ?region ?regionLabel {
                        SERVICE <https://query.wikidata.org/sparql> {
                            SELECT ?region ?regionLabel WHERE {
                                ?region wdt:P31 wd:Q82794 .
                                 # part of
                                ?region wdt:P361 <%s> .
                                SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en". }\s
                            }
                        }
                    }
                """.formatted(continent.getItem());
    }

    private String buildCountryQueryString(TupleContainer<String> region) {
        return """
                    PREFIX bd: <http://www.bigdata.com/rdf#>
                    PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                    PREFIX wd: <http://www.wikidata.org/entity/>
                    PREFIX wikibase: <http://wikiba.se/ontology#>
                
                    SELECT ?country ?countryLabel ?geoShape ?iso {
                        SERVICE <https://query.wikidata.org/sparql> {
                            SELECT ?country ?countryLabel ?geoShape ?iso {
                            ?country wdt:P31 wd:Q6256;      # Q6256 = country
                                            wdt:P361* <%s> ;
                                            wdt:P297 ?iso .
                            OPTIONAL { ?country wdt:P3896 ?geoShape }
                            SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en". }
                            }
                        }
                    }
                """.formatted(region.getItems().getValue0());
    }

    private String getDistrictsCountries(TupleContainer<String> country) {
        return
                """
                        PREFIX bd: <http://www.bigdata.com/rdf#>
                        PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                        PREFIX wd: <http://www.wikidata.org/entity/>
                        PREFIX wikibase: <http://wikiba.se/ontology#>
                        
                        SELECT ?district ?districtLabel ?iso ?osmId {
                            SERVICE <https://query.wikidata.org/sparql> {
                                SELECT ?district ?districtLabel ?iso ?osmId WHERE {
                                   <%s> wdt:P150 ?district .  # Germany contains these administrative territorial entities
                                   ?district wdt:P300 ?iso .
                                   ?district wdt:P402 ?osmId .
                                   OPTIONAL { ?district wdt:P3896 ?geoShape . }
                                  SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en". }
                                }
                            }
                        }
                        """.formatted(country.getItem());
    }

    @Cacheable(value = "regionQueries", key = "#regionType + '-' + #parent")
    public void getQueryHandlerCached(
            List<Map.Entry<RegionTypes, Function<TupleContainer<String>, String>>> regionsHierarchy,
            RegionTypes regionType,
            Optional<Value> parent,
            Optional<Value> superParent
    ) {
        getQueryHandlerWithRetry(regionsHierarchy, regionType, parent, superParent);
    }


    private void getQueryHandlerWithRetry(
            List<Map.Entry<RegionTypes, Function<TupleContainer<String>, String>>> regionsHierarchy,
            RegionTypes regionType,
            Optional<Value> parent,
            Optional<Value> superParent
    ) {
        Retry retry = Retry.of(
                "queryHandlerRetry",
                RetryConfig.custom()
                        .maxAttempts(3)
                        .waitDuration(REQUEST_TIMEOUT)
                        .retryOnException(e -> e instanceof QueryEvaluationException)
                        .build()
        );

        try (RepositoryConnection connection = repository.getConnection()) {
            Runnable runnable = Retry.decorateRunnable(
                    retry,
                    () -> {
                        rateLimiter.acquire(); // assuming rateLimiter is defined elsewhere
                        getQueryHandler(connection, regionsHierarchy, regionType, parent, superParent);
                    }
            );

            try {
                runnable.run();
            } catch (Exception e) {
                logger.error("Failed to get query handler for region type {}", regionType, e);
                throw new RuntimeException(e);
            }
        }
    }

    private TupleQueryResult reduceTurtleQueryCapacity(TupleQueryResult result, RegionTypes regionType) {
        List<BindingSet> allResults = QueryResults.asList(result);

        if (!isTestRun) {
            return new MutableTupleQueryResult(result.getBindingNames(), allResults);
        }

        if (
//                regionType == RegionTypes.CONTINENT_REGION
                regionType == RegionTypes.COUNTRY
                        || regionType == RegionTypes.DISTRICT) {
            allResults = allResults.stream().limit(queryNumberLimit).toList();
        }
        return new MutableTupleQueryResult(result.getBindingNames(), allResults);
    }


    private void getQueryHandler(
            RepositoryConnection connection,
            List<Map.Entry<RegionTypes, Function<TupleContainer<String>, String>>> regionsHierarchy,
            RegionTypes regionType,
            Optional<Value> parent,
            Optional<Value> superParent
    ) {
        var parentValue = parent.map(Value::stringValue).orElse(null);
        var superParentValue = superParent.map(Value::stringValue).orElse(null);
        String query = queries.get(regionType).apply(new TupleContainer<>(parentValue, superParentValue));
        TupleQuery tupleQuery = connection.prepareTupleQuery(query);
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            Timer.Sample sample = Timer.start();
            List<BindingSet> allResults = new ArrayList<>();
            try (TupleQueryResult result = reduceTurtleQueryCapacity(tupleQuery.evaluate(), regionType)) {
                if (regionType == RegionTypes.POI) {
                    return;
                }
                while (result.hasNext()) {
                    allResults.add(result.next());
                }

                Lists.partition(allResults, 50).parallelStream()
                        .forEach(batch -> {
                    createRegionFromQueryResults(
                            connection,
                            batch,
                            regionsHierarchy,
                            parentValue,
                            regionType
                    );
                });
            } finally {
                executorService.shutdown();
                sample.stop(processingTime);
                // Log the current metrics
                logger.info("Total requests: {}", regionsProcessed.count());
                logger.info("Average processing time: {} ms",
                        processingTime.mean(TimeUnit.MILLISECONDS));

            }
        });

        try {
            future.get(20, TimeUnit.MINUTES);
        } catch (Exception e) {
            logger.error("Query processing timed out or failed for region type: {}", regionType, e);
            throw new RuntimeException("Query processing failed", e);
        }

    }

    private void createRegionFromQueryResults(
            RepositoryConnection connection,
            List<BindingSet> results,
            List<Map.Entry<RegionTypes, Function<TupleContainer<String>, String>>> regionsHierarchy,
            String parent,
            RegionTypes regionType
    ) {
        for (BindingSet result : results) {
            SimpleRegionDto region = new SimpleRegionDto();
            IRI parentIri = parent != null ? factory.createIRI(parent) : null;
            region.setSourceParent(parentIri);
            region.setType(regionType);
            Value entity = null;

            for (Binding binding : result) {
                if (entity == null) {
                    entity = binding.getValue();
                    region.setSource(factory.createIRI(entity.stringValue()));
                }
                processBinding(binding, region);
            }

            // Create region
            try {
                if (regionService.getRegion(region.getId()).isEmpty()) {
                    regionService.createRegion(region, true);
                    logger.info("Created region {}", region.getId());
                } else {
                    logger.warn("Region {} already exists", region.getId());
                }
            } catch (Exception e) {
                logger.error("Failed to create region: {}", region.getId(), e);
            }

            // Process next hierarchy level if needed
            Value finalEntity = entity;
            if (shouldProcessNextHierarchyLevel(regionsHierarchy, finalEntity)) {
                processNextHierarchyLevel(connection, regionsHierarchy, finalEntity, parentIri);
            }
        }
    }

    private void processBinding(Binding binding, SimpleRegionDto region) {
        String name = binding.getName().toLowerCase();
        if (name.contains("label")) {
            region.setName(binding.getValue().stringValue());
            region.setId(binding.getValue().stringValue().replaceAll(" ", ""));
        } else if (name.contains("geoshape")) {
            region.setGeoShape(factory.createIRI(binding.getValue().stringValue()));
        } else if (name.contains("osmid")) {
            region.setOsmId(binding.getValue().stringValue());
        } else if (name.contains("iso")) {
            region.setIso(binding.getValue().stringValue());
        }
    }

    private boolean shouldProcessNextHierarchyLevel(
            List<Map.Entry<RegionTypes, Function<TupleContainer<String>, String>>> regionsHierarchy,
            Value entity) {
        return !regionsHierarchy.isEmpty() && entity != null;
    }

    private void processNextHierarchyLevel(
            RepositoryConnection connection,
            List<Map.Entry<RegionTypes, Function<TupleContainer<String>, String>>> regionsHierarchy,
            Value entity,
            Value superParent
    ) {
        List<Map.Entry<RegionTypes, Function<TupleContainer<String>, String>>> remainingRegions =
                regionsHierarchy.subList(1, regionsHierarchy.size());

        if (!remainingRegions.isEmpty()) {
            RegionTypes nextRegionType = remainingRegions.getFirst().getKey();
            getQueryHandler(
                    connection,
                    remainingRegions,
                    nextRegionType,
                    Optional.of(entity),
                    Optional.ofNullable(superParent)
            );
        }
    }

    private void outputListsToFiles(String currentDistrictUri, List<POIClass> pois, List<POIClass> optimized, String type) {
        try {
            Path dir = Paths.get("./aco");
            Files.createDirectories(dir);

            Path filePathAco = dir.resolve("aco-" + Arrays.stream(currentDistrictUri.split("/")).toList().getLast() + ".txt");
            Path filePathNon = dir.resolve("nonaco-" + Arrays.stream(currentDistrictUri.split("/")).toList().getLast() + ".txt");
            Files.write(filePathAco, optimized.stream().map(POIClass::toString).toList(), StandardCharsets.UTF_8);
            Files.write(filePathNon, pois.stream().map(POIClass::toString).toList(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            logger.error("Failed to write optimized POIs to file", exception);
        }
    }

    private void createPOIsInRdf(List<POIClass> pois) {
//        List<IRI> iris = pois.stream()
//                .filter(Objects::nonNull)
//                .map(regionService::createPOI).toList();

        List<POIClass> nonPresentPois = pois.stream()
                .filter(poi -> {
                    try {
                        synchronized (rdf4JTemplate) {
                            return regionService.getPOI(poi.getId()).isEmpty();
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to get region for POI {}", poi.getId(), e);
                        return false;
                    }
                }).toList();
        List<IRI> iris = regionService.createPOIs(nonPresentPois);
        logger.info("Created {} POIs in RDF", iris.size());
    }

    private void makeQueryForPOIs() {
        List<Pair<IRI, IRI>> allDistricts = regionDao.listByTypeIdWithChild(RegionTypes.DISTRICT)
                .stream().filter(i -> !regionDao.isRegionComplete(i.getValue0())).toList();

        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                processors,
                processors * 2,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        try {
            int totalBatches = (allDistricts.size() + WIKIDATA_BATCH_SIZE - 1) / WIKIDATA_BATCH_SIZE;
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
                int start = batchIndex * WIKIDATA_BATCH_SIZE;
                int end = Math.min(start + WIKIDATA_BATCH_SIZE, allDistricts.size());
                List<Pair<IRI, IRI>> currentBatch = allDistricts.subList(start, end);

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        try (RepositoryConnection connection = repository.getConnection()) {
                            processBatch(currentBatch, connection);
                        }
                    } catch (Exception e) {
                        logger.error("Error processing batch starting at index {} and ending at index {}", start, end, e);
                    }
                }, executor);

                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
//        IntStream.range(0, (allDistricts.size() + WIKIDATA_BATCH_SIZE - 1) / WIKIDATA_BATCH_SIZE)
//                .parallel()
//                .forEach(batchIndex -> {
//                   int start = batchIndex * WIKIDATA_BATCH_SIZE;
//                   int end = Math.min(start + WIKIDATA_BATCH_SIZE, allDistricts.size());
//                   List<Pair<IRI, IRI>> currentBatch = allDistricts.subList(start, end);
//                   processBatch(currentBatch);
//                });
    }


    private void processBatch(List<Pair<IRI, IRI>> currentBatch, RepositoryConnection connection) {
        if (currentBatch.isEmpty()) {
            return;
        }

        try {
            List<String> districtUrisForQuery = currentBatch
                    .stream()
                    .map(pair -> pair.getValue1().stringValue())
                    .toList();
            String wikidataQueryString = buildPoiQueryString(districtUrisForQuery);
            Map<String, List<POIClass>> poisFromWikidata = new HashMap<>();

            // wikidata access step
            TupleQuery tupleQuery = connection.prepareTupleQuery(wikidataQueryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                transformQueryResultsToPOIs(result, poisFromWikidata);
            }

            List<String> wikidataPois = new ArrayList<>();
            for (List<POIClass> list : poisFromWikidata.values()) {
                wikidataPois.addAll(list.stream().map(POIClass::getSource).map(IRI::stringValue).toList());
            }

            // dbpedia access step
            if (!wikidataPois.isEmpty()) {
                for (int j = 0; j < wikidataPois.size(); j += DBPEDIA_BATCH_SIZE) {
                    List<String> wikidataPoisForQuery = wikidataPois.subList(j, Math.min(j + DBPEDIA_BATCH_SIZE, wikidataPois.size()));
                    if (wikidataPoisForQuery.isEmpty()) {
                        continue;
                    }

                    String dbpediaQueryString = buildPOIDBPediaQueryString(wikidataPoisForQuery);
                    TupleQuery tupleQueryDbpedia = connection.prepareTupleQuery(dbpediaQueryString);

                    try (TupleQueryResult resultPedia = tupleQueryDbpedia.evaluate()) {
                        transformDBPediaResults(resultPedia, poisFromWikidata);
                    } catch (QueryEvaluationException exception) {
                        logger.error("Failed to transform DBPedia results for a POI sub-batch (size: {}) in batch starting with district: {}",
                                wikidataPoisForQuery.size(), districtUrisForQuery.getFirst(), exception);
                    }
                }
            } else {
                logger.info("No POIS found in wikidata disticts {}", currentBatch);
            }

            // ACO optimization step
            for (Pair<IRI, IRI> district : currentBatch) {
                String currentDistrictUri = district.getValue1().stringValue();
                if (!poisFromWikidata.containsKey(currentDistrictUri) || poisFromWikidata.get(currentDistrictUri).isEmpty()) {
                    continue;
                }
                HashMap<String, POIClass> uniquePois = new HashMap<>();
                poisFromWikidata.get(currentDistrictUri).forEach(poi -> uniquePois.put(poi.getSource().stringValue(), poi));
                List<POIClass> pois = uniquePois.values().stream().toList();
                OptionalInt maxSiteLinks = pois.stream().mapToInt(POIClass::getSiteLinks).max();
                OptionalInt maxOutgoingLinks = pois.stream().mapToInt(POIClass::getOutgoingLinks).max();
                OptionalInt maxStatements = pois.stream().mapToInt(POIClass::getStatements).max();

                POIClass.setOUTGOING_LINKS_MAX(maxOutgoingLinks.orElse(0));
                POIClass.setSITE_LINKS_MAX(maxSiteLinks.orElse(0));
                POIClass.setSTATEMENTS_MAX(maxStatements.orElse(0));

                logger.info("Optimizing {} POIs for district {}", pois.size(), currentDistrictUri);
                ACOHyperparameters hyperparameters = ACOHyperparameters.getDefault();
                hyperparameters.setSelectionSize(Math.round((double) pois.size() / 4));
                AntColonyOptimizer optimizer = new AntColonyOptimizer(pois, hyperparameters);
                List<POIClass> optimized = Arrays.stream(optimizer.optimize()).toList();
                logger.info("Finished optimization for district {}. Optimized: {}, Original in batch for district: {}",
                        currentDistrictUri, optimized.size(), pois.size());
                try {
                    createPOIsInRdf(optimized);
                    regionDao.signalChildrenCompletion(district.getValue0());
                } catch (Exception exception) {
                    logger.error("Failed to create POIs in RDF", exception);
                }
            }
        } catch (Exception exception) {
            logger.error("Failed to process batch of POIs", exception);
        }
    }

    public void updateKGOntologies() {
        regionService.updateAllOntologiesPOIs();
    }

    private void makeQueryForPOIsWithRetry() {
        Retry retry = Retry.of(
                "queryHandlerRetry",
                RetryConfig.custom()
                        .maxAttempts(3)
                        .waitDuration(REQUEST_TIMEOUT)
                        .retryOnException(e -> e instanceof QueryEvaluationException)
                        .build()
        );

        Runnable runnable = Retry.decorateRunnable(
                retry,
                () -> {
                    rateLimiter.acquire();
                    makeQueryForPOIs();
                }
        );

        try {
            runnable.run();
        } catch (Exception e) {
            logger.error("Failed to get query handler for POIs", e);
        }
    }

    private void transformDBPediaResults(TupleQueryResult results, Map<String, List<POIClass>> poisFromWikidata) {
        results.forEach(bindings -> {
            if (bindings.getValue("wikidataPoiUri") == null) {
                return;
            }
            List<POIClass> poiList = poisFromWikidata.values().stream().flatMap(Collection::stream).toList();
            Optional<POIClass> poi = poiList.stream().filter(p -> p.getSource().stringValue().contains(bindings.getValue("wikidataPoiUri").stringValue())).findFirst();
            Value outDegree = bindings.getValue("outDegree");
            if (outDegree != null && poi.isPresent()) {
                poi.get().setOutgoingLinks(Integer.parseInt(outDegree.stringValue()));
            }

            Value thumb = bindings.getValue("thumb");
            if (thumb != null && poi.isPresent()) {
                Pair<String, String> images = poi.get().getImages();
                poi.get().setImages(images.getValue0(), thumb.stringValue());
            }
        });
    }

    private void transformQueryResultsToPOIs(TupleQueryResult results, Map<String, List<POIClass>> poisFromWikidata) {
        results.forEach(bindings -> {
            POIClass poi = new POIClass();

            // setup simple region dto base
            String parentValue = bindings.getValue("district").stringValue();
            Value poiValue = bindings.getValue("poi");
            poi.setSource(factory.createIRI(poiValue.stringValue()));
            Value poiLabelValue = bindings.getValue("poiLabel");
            poi.setName(poiLabelValue.stringValue());
            poi.setId(URIHandling.toSafeIRIForm(poiLabelValue.stringValue()));
            poi.setSourceParent(parentValue != null ? factory.createIRI(parentValue) : null);
            poi.setType(RegionTypes.POI);


            // setup poi class fields
            Value typeValue = bindings.getValue("type");
            poi.setFeature(WIKIDATA.RegionOntology.QTypes.getQTypeFromIRI(typeValue.stringValue()));
            Value officialWebsiteValue = bindings.getValue("officialWebsite");
            poi.setOfficialWebsite(officialWebsiteValue != null ? officialWebsiteValue.stringValue() : null);
            Value coordValue = bindings.getValue("coord");
            poi.setCoords(coordValue != null ? coordValue.stringValue() : null);

            // Set statements count
            Value statementsValue = bindings.getValue("statementCount");
            if (statementsValue != null) {
                poi.setStatements(Integer.parseInt(statementsValue.stringValue()));
            } else {
                poi.setStatements(0);
            }

            // Set images from both Wikidata and DBpedia
            Value wikidataImage = bindings.getValue("image");
            Value dbpediaThumb = bindings.getValue("thumb");
            poi.setImages(
                    wikidataImage != null ? wikidataImage.stringValue() : null,
                    dbpediaThumb != null ? dbpediaThumb.stringValue() : null
            );

            // Set OSM link
            Value osmId = bindings.getValue("osmId");
            if (osmId != null) {
                poi.setOsmLink("https://www.openstreetmap.org/" + osmId.stringValue());
            }

            Value siteLinks = bindings.getValue("siteLinks");
            if (siteLinks != null) {
                poi.setSiteLinks(Integer.parseInt(siteLinks.stringValue()));
            }

            // Set internet mentions
            poi.setInternetMentions(
                    getStringValueOrNull(bindings, "quoraTopicID"),
                    getStringValueOrNull(bindings, "tripAdvisorID"),
                    getStringValueOrNull(bindings, "twitterUsername"),
                    getStringValueOrNull(bindings, "imdbKeywordID")
            );

            if (poisFromWikidata.containsKey(parentValue)) {
                poisFromWikidata.get(parentValue).add(poi);
            } else {
                poisFromWikidata.put(parentValue, new ArrayList<>(List.of(poi)));
            }
        });
    }

    private String getStringValueOrNull(BindingSet bindings, String name) {
        Value value = bindings.getValue(name);
        return value != null ? value.stringValue() : null;
    }

    private void fetchTopMaps(RegionTypes type) {
        logger.info("Starting to fetch top maps for region type: {}", type);

        // get continents
        List<RegionDto> topRegions = regionDao.listAllByType(type).stream()
                .distinct()
                .toList();;
        logger.debug("Found {} regions of type {}", topRegions.size(), type);

        for (RegionDto region : topRegions) {
            logger.debug("Processing region: {}", region.id());

//            if (fetchMapFromWikimedia(type, region)) {
//                logger.debug("Successfully fetched map from Wikimedia for region: {}", region.id());
//                continue;
//            }
//            logger.debug("Wikimedia fetch failed, trying alternative approach for region: {}", region.id());

            List<RegionDto> countries = regionDao
                    .listAllCountriesForRegion(region.id())
                    .stream()
                    .map(countryIRI -> regionDao.getByIdSafe(countryIRI))
                    .toList();

            logger.debug("Found {} countries for region {}", countries.size(), region.id());

            if (countries.isEmpty()) {
                logger.debug("No countries found for region {}, skipping", region.id());
                continue;
            }

            logger.info("Fetching map data from Overpass for {} countries in region {}", countries.size(), region.id());
            String mapInfo = overpassService.runQuerySuperRegion(
                    countries.stream().map(RegionDto::getIso).toList(),
                    countries.stream().map(RegionDto::getName).toList()
            );

            logger.debug("Saving geoJSON for region {}", region.id());
            overpassService.saveRegionGeoJson(mapInfo, type, region.getId().getLocalName());
            logger.info("Successfully saved map for region {}", region.id());
        }

        logger.info("Completed fetching top maps for region type: {}", type);
    }


    private void fetchBottomMaps(RegionTypes type) {
        logger.info("Starting to fetch bottom maps for region type: {}", type);

        List<RegionDto> bottomRegions = regionDao
                .listAllByType(type)
                .stream()
                .distinct()
                .toList();
        logger.debug("Found {} regions of type {}", bottomRegions.size(), type);

        for (RegionDto region : bottomRegions) {
            logger.debug("Processing region: {} ({})", region.getId().getLocalName(), region.getIso());

            if (fetchMapFromWikimedia(type, region)) {
                logger.info("Successfully fetched map from Wikimedia for region: {}", region.getId());
                continue;
            }
            logger.debug("Wikimedia fetch failed, proceeding with Overpass for region: {}", region.getId());

            if (type == RegionTypes.COUNTRY) {
                logger.info("Processing as country: {}", region.getIso());
                String mapInfo = overpassService.runQueryCountry(region.getIso(), region.getName());
                logger.debug("Retrieved Overpass data for country {}, saving geoJSON", region.getIso());
                overpassService.saveRegionGeoJson(mapInfo, type, region.getId().getLocalName());
                logger.info("Successfully saved country map for {}", region.getId());
            } else {
                logger.debug("Processing as sub-region, fetching parent region for {}", region.getId());
                RegionDto parentRegion = regionDao.getByIdSafe(region.getParentRegion());

                if (parentRegion == null) {
                    logger.warn("Parent region not found for {}", region.getId());
                    continue;
                }

                logger.info("Fetching district map for {} (parent: {})",
                        region.getIso(), parentRegion.getIso());
                String mapInfo = overpassService.runQueryDistrict(region.getOsmId(), parentRegion.getIso(), region.getName());
                logger.debug("Retrieved Overpass data for district {}, saving geoJSON", region.getIso());
                overpassService.saveRegionGeoJson(mapInfo, type, region.getId().getLocalName());
                logger.info("Successfully saved district map for {}", region.getId());
            }
        }

        logger.info("Completed fetching bottom maps for region type: {}", type);
    }

    private boolean fetchMapFromWikimedia(RegionTypes type, RegionDto region) {
        if (overpassService.regionGeoJsonExists(type, region.id.getLocalName())) {
            return true;
        }

        if (region.getGeoShape() != null) {
            try {
                String mapInfo = overpassService.runWikimediaQuery(region.getGeoShape().stringValue());
                if (mapInfo == null) {
                    return false;
                }
                overpassService.saveRegionGeoJson(mapInfo, type, region.getId().getLocalName(), true);
                return true;
            } catch (Exception exception) {
                logger.warn("Cannot fetch using geoshape. Rolling to countries iso method", exception);
            }
        }
        return false;
    }


    public void fetchAllMaps() {
        fetchTopMaps(RegionTypes.CONTINENT);
//        fetchTopMaps(RegionTypes.CONTINENT_REGION);
        fetchBottomMaps(RegionTypes.COUNTRY);
        fetchBottomMaps(RegionTypes.DISTRICT);
    }


    public void addAllRegionsToRepository() {
        if (versionDao.hasRegionVersion(regionVersion)) {
            logger.info("Repository already has all the regions");
            return;
        }

        circuitBreaker.executeRunnable(() -> {
            self.getQueryHandlerCached(
                    queries.entrySet().stream().toList(),
                    RegionTypes.WORLD,
                    Optional.empty(),
                    Optional.empty()
            );
            logger.info("Repository already has all the regions");
        });

        versionDao.saveRegionVersion(factory.createIRI(WIKIDATA.SPARQL_ENDPOINT.toString()), regionVersion);
    }


    public void addAllPOIs() {
        if (versionDao.hasPOIVersion(poiVersion)) {
            logger.info("Repository already has all the POIs");
            return;
        }


        circuitBreaker.executeRunnable(this::makeQueryForPOIsWithRetry);
        versionDao.savePOIVersion(List.of(
                factory.createIRI(WIKIDATA.SPARQL_ENDPOINT.toString()),
                factory.createIRI(DBPEDIA.RDF)), poiVersion);
//        regionService.normalizeFeatureScores();
    }
}