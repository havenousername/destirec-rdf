package org.destirec.destirec.rdf4j.knowledgeGraph;

import com.google.common.util.concurrent.RateLimiter;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.destirec.destirec.rdf4j.region.RegionDao;
import org.destirec.destirec.rdf4j.region.RegionService;
import org.destirec.destirec.rdf4j.region.apiDto.SimpleRegionDto;
import org.destirec.destirec.rdf4j.version.VersionDao;
import org.destirec.destirec.rdf4j.vocabulary.DBPEDIA;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA;
import org.destirec.destirec.utils.SafeIRI;
import org.destirec.destirec.utils.rdfDictionary.RegionNames.Individuals.RegionTypes;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.impl.MutableTupleQueryResult;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.javatuples.Pair;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KnowledgeGraphService {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected final RDF4JTemplate rdf4JTemplate;
    protected final RegionService regionService;
    private final Map<RegionTypes, Function<String, String>> queries;
    private final SimpleValueFactory factory = SimpleValueFactory.getInstance();
    private final VersionDao versionDao;
    private final RateLimiter rateLimiter;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(2);
    private static final int MAX_REQUESTS_PER_SECOND = 10;
    private static final int MAX_REQUESTS_PER_SECOND_POI = 2;
    private final CircuitBreaker circuitBreaker = CircuitBreaker.of("wikidata-api",
            CircuitBreakerConfig.custom()
                    .failureRateThreshold(50)
                    .waitDurationInOpenState(Duration.ofMinutes(1))
                    .build());

    @Lazy
    @Autowired
    private KnowledgeGraphService self;
    @Autowired
    private RegionDao regionDao;

    private boolean isTestRun = true;


    public KnowledgeGraphService(RDF4JTemplate rdf4JTemplate, RegionService regionService, VersionDao versionDao) {
        this.rdf4JTemplate = rdf4JTemplate;
        this.regionService = regionService;
        this.versionDao = versionDao;
        queries = new LinkedHashMap<>();

        queries.put(RegionTypes.WORLD, this::buildWorldQueryString);
        queries.put(RegionTypes.CONTINENT, this::buildContinentQueryString);
        queries.put(RegionTypes.CONTINENT_REGION, this::buildContinentRegionQueryString);
        queries.put(RegionTypes.COUNTRY, this::buildCountyQueryString);
        queries.put(RegionTypes.DISTRICT, this::getDistrictsCountries);
//        queries.put(RegionTypes.POI, this::getPOIs);

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
                
                        SELECT ?poi ?poiLabel ?officialWebsite ?image ?coord
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
                                wd:Q4421    # FOREST
                                wd:Q472972  # NATURAL_RESERVE
                                wd:Q623578  # CANYON
                                wd:Q839954  # HISTORIC_DISTRICT
                                wd:Q41176   # CATHEDRAL
                                wd:Q124714  # CASTLE
                                wd:Q209939  # HIKING_TRAIL
                                wd:Q1779811 # CLIMBING_ROUTE
                                wd:Q207326  # LOOKOUT_POINT
                                wd:Q210327  # SNOWBOARDING
                                wd:Q54202   # SKIING
                                wd:Q180809  # SKI_JUMPING
                                wd:Q1506654 # SLEDDING
                                wd:Q875538  # SKI_RESORT
                                wd:Q173211  # ICE_CLIMBING
                                wd:Q2202162 # SNOW_PARK
                                wd:Q1493709 # AMUSEMENT_PARK
                                wd:Q1824207 # THEME_PARK
                                wd:Q1735272 # KART_RACING_TRACK
                                wd:Q1407358 # SHOOTING_RANGE
                                wd:Q133357  # ARCADE
                                wd:Q28154028# ESCAPE_ROOM
                                wd:Q183424  # FESTIVAL_VENUE
                                wd:Q27017155# ICE_CREAM_SHOP
                                wd:Q1502956 # BOWLING_ALLEY
                                wd:Q1324011 # BEER_GARDEN
                                wd:Q131734  # BREWERY
                                wd:Q11707   # RESTAURANT
                                wd:Q18119866# STREET_FOOD_VENUE
                                wd:Q210272  # FOOD_MARKET
                                wd:Q55488   # SHOPPING_MALL
                                wd:Q18534524# SOUVENIR_SHOP
                                wd:Q3305213 # MARKET
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

    private String buildWorldQueryString(String parent) {
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

    private String buildContinentQueryString(String noName) {
        return """
                    PREFIX bd: <http://www.bigdata.com/rdf#>
                    PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                    PREFIX wd: <http://www.wikidata.org/entity/>
                    PREFIX wikibase: <http://wikiba.se/ontology#>
                
                    SELECT ?continent ?continentLabel {
                        SERVICE <https://query.wikidata.org/sparql> {
                            SELECT ?continent ?continentLabel WHERE {
                                ?continent wdt:P31 wd:Q5107 .
                                ?continent wdt:P361 wd:Q2 .
                                SERVICE wikibase:label {
                                    bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en" .
                                }
                            }
                        }
                    }
                """;
    }

    private String buildContinentRegionQueryString(String continent) {
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
                """.formatted(continent);
    }

    private String buildCountyQueryString(String region) {
        return """
                    PREFIX bd: <http://www.bigdata.com/rdf#>
                    PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                    PREFIX wd: <http://www.wikidata.org/entity/>
                    PREFIX wikibase: <http://wikiba.se/ontology#>
                
                    SELECT ?country ?countryLabel {
                        SERVICE <https://query.wikidata.org/sparql> {
                            SELECT ?country ?countryLabel WHERE {
                                {
                        ?country wdt:P31 wd:Q15239622 ;  # Q15239622 = continent region
                                        wdt:P361 <%s> .     # Q27381 = Eurasia
                      }
                      UNION
                      {
                        ?country wdt:P31 wd:Q6256 ;      # Q6256 = country
                                        wdt:P361 <%s> .
                      }
                      SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en". }
                            }
                        }
                    }
                """.formatted(region, region);
    }

    private String getDistrictsCountries(String country) {
        return
                """
                        PREFIX bd: <http://www.bigdata.com/rdf#>
                        PREFIX wdt: <http://www.wikidata.org/prop/direct/>
                        PREFIX wd: <http://www.wikidata.org/entity/>
                        PREFIX wikibase: <http://wikiba.se/ontology#>
                        
                        SELECT ?district ?districtLabel {
                            SERVICE <https://query.wikidata.org/sparql> {
                                SELECT ?district ?districtLabel WHERE {
                                   <%s> wdt:P150 ?district .  # Germany contains these administrative territorial entities
                                  SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en". }
                                }
                            }
                        }
                        """.formatted(country);
    }

    @Cacheable(value = "regionQueries", key = "#regionType + '-' + #parent")
    public void getQueryHandlerCached(
            List<Map.Entry<RegionTypes, Function<String, String>>> regionsHierarchy,
            RegionTypes regionType,
            Optional<Value> parent
    ) {
        getQueryHandlerWithRetry(regionsHierarchy, regionType, parent);
    }


    private void getQueryHandlerWithRetry(
            List<Map.Entry<RegionTypes, Function<String, String>>> regionsHierarchy,
            RegionTypes regionType,
            Optional<Value> parent
    ) {
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
                    rateLimiter.acquire(); // assuming rateLimiter is defined elsewhere
                    getQueryHandler(regionsHierarchy, regionType, parent);
                }
        );

        try {
            runnable.run();
        } catch (Exception e) {
            logger.error("Failed to get query handler for region type {}", regionType, e);
            throw new RuntimeException(e);
        }
    }

    private TupleQueryResult reduceTurtleQueryCapacity(TupleQueryResult result, RegionTypes regionType) {
        List<BindingSet> allResults = org.eclipse.rdf4j.query.QueryResults.asList(result);

        if (regionType == RegionTypes.CONTINENT_REGION
                || regionType == RegionTypes.COUNTRY
                || regionType == RegionTypes.DISTRICT) {
            allResults = allResults.stream().limit(2).toList();
        }
        return new MutableTupleQueryResult(result.getBindingNames(), allResults);
    }


    private void getQueryHandler(
            List<Map.Entry<RegionTypes, Function<String, String>>> regionsHierarchy,
            RegionTypes regionType,
            Optional<Value> parent
    ) {
        rdf4JTemplate.consumeConnection(connection -> {
            var parentValue = parent.map(Value::stringValue).orElse(null);
            String query = queries.get(regionType).apply(parentValue);
            TupleQuery tupleQuery = connection.prepareTupleQuery(query);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                if (RegionTypes.POI != regionType) {
                    createRegionFromQueryResults(
                            isTestRun ? reduceTurtleQueryCapacity(result, regionType) : result,
                            regionsHierarchy,
                            parentValue,
                            regionType
                    );
                }
            }
        });
    }

    private void createRegionFromQueryResults(
            TupleQueryResult results,
            List<Map.Entry<RegionTypes, Function<String, String>>> regionsHierarchy,
            String parent,
            RegionTypes regionType
    ) {
        results.forEach(value -> {
            SimpleRegionDto region = new SimpleRegionDto();
            IRI parentIri = parent != null ? factory.createIRI(parent) : null;
            region.setSourceParent(parentIri);
            region.setType(regionType);
            Value entity = null;

            for (Binding binding : value) {
                if (entity == null) {
                    entity = binding.getValue();
                    region.setSource(factory.createIRI(
                            entity.stringValue()));
                }
                if (binding.getName().toLowerCase().contains("label")) {
                    region.setName(binding.getValue().stringValue());
                    region.setId(binding.getValue().stringValue().replaceAll(" ", ""));
                }
            }

            regionService.createRegion(region, true);
            if (!regionsHierarchy.isEmpty()) {
                List<Map.Entry<RegionTypes, Function<String, String>>> remainingRegions =
                        regionsHierarchy.subList(1, regionsHierarchy.size());

                if (!remainingRegions.isEmpty()) {
                    RegionTypes nextRegionType = remainingRegions.getFirst().getKey();
                    if (entity != null) {
                        getQueryHandler(
                                remainingRegions,
                                nextRegionType,
                                Optional.of(entity)
                        );
                    }
                }

            }
        });
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
        List<IRI> iris = pois.stream().map(regionService::createPOI).toList();
        logger.info("Created {} POIs in RDF", iris.size());
    }

    private void makeQueryForPOIs() {
        List<Pair<IRI, IRI>> allDistricts = regionDao.listByType(RegionTypes.DISTRICT);
        int batchSize = 100;
        rdf4JTemplate.consumeConnection(connection -> {
            for (int i = 0; i < allDistricts.size(); i += batchSize) {
                List<Pair<IRI, IRI>> currentBatch = allDistricts.subList(i, Math.min(i + batchSize, allDistricts.size()));
                if (currentBatch.isEmpty()) {
                    continue;
                }
                List<String> districtUrisForQuery = currentBatch
                        .stream()
                        .map(pair -> pair.getValue1().stringValue())
                        .toList();

                String wikidataQueryString = buildPoiQueryString(districtUrisForQuery);
                Map<String, List<POIClass>> poisFromWikidata = new HashMap<>();

                TupleQuery tupleQuery = connection.prepareTupleQuery(wikidataQueryString);
                TupleQueryResult result = null;
                try {
                    result = tupleQuery.evaluate();
                    transformQueryResultsToPOIs(result, poisFromWikidata);
                } finally {
                    if (result != null) {
                        result.close();
                    }
                }

                List<String> wikidataPois = new ArrayList<>();
                for (List<POIClass> list : poisFromWikidata.values()) {
                    wikidataPois.addAll(list.stream().map(POIClass::getSource).map(IRI::stringValue).toList());
                }

                String dbpediaQueryString = buildPOIDBPediaQueryString(wikidataPois);
                TupleQuery tupleQueryDbpedia = connection.prepareTupleQuery(dbpediaQueryString);
                TupleQueryResult resultPedia = null;
                try {
                    resultPedia = tupleQueryDbpedia.evaluate();
                    transformDBPediaResults(resultPedia, poisFromWikidata);
                } finally {
                    if (resultPedia != null) {
                        result.close();
                    }
                }


                for (Pair<IRI, IRI> district : currentBatch) {
                    String currentDistrictUri = district.getValue1().stringValue();
                    if (!poisFromWikidata.containsKey(currentDistrictUri) || poisFromWikidata.get(currentDistrictUri).isEmpty()) {
                        continue;
                    }
                    HashMap<String, POIClass> uniquePois = new HashMap<>();
                    poisFromWikidata.get(currentDistrictUri).forEach(poi -> uniquePois.put(poi.getSource().stringValue(), poi));
                    List<POIClass> pois = uniquePois.values().stream().toList();
                    logger.info("Optimizing {} POIs for district {}", pois.size(), currentDistrictUri);
                    ACOHyperparameters hyperparameters = ACOHyperparameters.getDefault();
                    hyperparameters.setSelectionSize(Math.round((double) pois.size() / 4));
                    AntColonyOptimizer optimizer = new AntColonyOptimizer(pois, hyperparameters);
                    List<POIClass> optimized = optimizer.optimize();
                    logger.info("Finished optimization for district {}. Optimized: {}, Original in batch for district: {}",
                            currentDistrictUri, optimized.size(), pois.size());
                    createPOIsInRdf(optimized);
                }
            }
            regionService.updateAllOntologiesPOIs();
        });
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
            throw new RuntimeException(e);
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
            poi.setId(SafeIRI.toSafeIRIForm(poiLabelValue.stringValue()));
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

    public void addAllRegionsToRepository() {
        if (versionDao.hasRegionVersion()) {
            logger.info("Repository already has all the regions");
            return;
        }

        circuitBreaker.executeRunnable(() -> {
            self.getQueryHandlerCached(
                    queries.entrySet().stream().toList(),
                    RegionTypes.WORLD,
                    Optional.empty());
            logger.info("Repository already has all the regions");
        });
        versionDao.saveRegionVersion(factory.createIRI(WIKIDATA.SPARQL_ENDPOINT.toString()));
    }

    public void addAllPOIs() {
        if (versionDao.hasPOIVersion()) {
            logger.info("Repository already has all the POIs");
            return;
        }


        circuitBreaker.executeRunnable(this::makeQueryForPOIsWithRetry);
        versionDao.savePOIVersion(List.of(
                factory.createIRI(WIKIDATA.SPARQL_ENDPOINT.toString()),
                factory.createIRI(DBPEDIA.RDF)), 20);
    }
}