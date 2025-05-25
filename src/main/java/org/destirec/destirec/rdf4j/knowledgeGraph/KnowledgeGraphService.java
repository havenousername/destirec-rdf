package org.destirec.destirec.rdf4j.knowledgeGraph;

import com.google.common.util.concurrent.RateLimiter;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.NonNull;
import org.destirec.destirec.rdf4j.region.RegionService;
import org.destirec.destirec.rdf4j.region.apiDto.SimpleRegionDto;
import org.destirec.destirec.rdf4j.version.VersionDao;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA;
import org.destirec.destirec.utils.SafeIRI;
import org.destirec.destirec.utils.rdfDictionary.RegionNames.Individuals.RegionTypes;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

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

    @Lazy
    @Autowired
    private KnowledgeGraphService self;


    public KnowledgeGraphService(RDF4JTemplate rdf4JTemplate, RegionService regionService, VersionDao versionDao) {
        this.rdf4JTemplate = rdf4JTemplate;
        this.regionService = regionService;
        this.versionDao = versionDao;
        queries = new LinkedHashMap<>();

        queries.put(RegionTypes.WORLD, this::getWorld);
        queries.put(RegionTypes.CONTINENT, this::getContinentQuery);
        queries.put(RegionTypes.CONTINENT_REGION, this::getContinentRegions);
        queries.put(RegionTypes.COUNTRY, this::getRegionCountries);
        queries.put(RegionTypes.DISTRICT, this::getDistrictsCountries);
        queries.put(RegionTypes.POI, this::getPOIs);

        rateLimiter = RateLimiter.create(MAX_REQUESTS_PER_SECOND);
    }

    private String getPOIs(String district) {
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
                 ?osmId ?statementCount ?siteLinks ?dbpedia ?outDegree ?thumb
                 ?quoraTopicID ?tripAdvisorID ?twitterUsername ?imdbKeywordID
                WHERE {
                  SERVICE <https://query.wikidata.org/sparql> {
                    SELECT ?poi ?poiLabel ?officialWebsite ?image ?coord
                    ?osmId ?statementCount ?siteLinks ?dbpedia
                    ?quoraTopicID ?tripAdvisorID ?twitterUsername ?imdbKeywordID
                    WHERE {
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
                           wdt:P131 <%s> .  # Located in Berlin
        
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
        
                  # DBpedia enrichment using owl:sameAs
                  SERVICE <https://dbpedia.org/sparql> {
                    ?dbpedia owl:sameAs ?poi .
                    OPTIONAL { ?dbpedia dbo:wikiPageOutDegree ?outDegree }
                    OPTIONAL { ?dbpedia dbo:thumbnail ?thumb }
                  }
                }
                ORDER BY DESC(?statementCount)
        """.formatted(district);
    }

    private String getWorld(String parent) {
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

    private String getContinentQuery(String noName) {
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

    private String getContinentRegions(String continent) {
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

    private String getRegionCountries(String region) {
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

    private final CircuitBreaker circuitBreaker = CircuitBreaker.of("wikidata-api",
            CircuitBreakerConfig.custom()
                    .failureRateThreshold(50)
                    .waitDurationInOpenState(Duration.ofMinutes(1))
                    .build());



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
                if (RegionTypes.POI == regionType && parentValue != null) {
                    List<POIClass> pois = transformQueryResultsToPOIs(result, parentValue);
                    AntColonyOptimizer optimizer = new AntColonyOptimizer(
                            pois, ACOHyperparameters.getDefault());
                    List<POIClass> optimized = optimizer.optimize();
                } else if (RegionTypes.POI != regionType) {
                    createRegionFromQueryResults(
                            result,
                            regionsHierarchy,
                            parentValue,
                            regionType
                    );
                }
            }
        });
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

    private List<POIClass> transformQueryResultsToPOIs(TupleQueryResult results, @NonNull String parent) {
        List<POIClass> pois = new ArrayList<>();

        results.forEach(bindings -> {
            POIClass poi = new POIClass();

            // setup simple region dto base
            Value poiValue = bindings.getValue("poi");
            poi.setSource(factory.createIRI(poiValue.stringValue()));
            Value poiLabelValue = bindings.getValue("poiLabel");
            poi.setName(poiLabelValue.stringValue());
            poi.setId(SafeIRI.toSafeIRIForm(poiLabelValue.stringValue()));
            poi.setSourceParent(factory.createIRI(parent));
            poi.setType(RegionTypes.POI);


            // setup poi class fields
            Value typeValue = bindings.getValue("type");
            poi.setFeature(WIKIDATA.RegionOntology.QTypes.valueOf(typeValue.stringValue()));
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

            // Set outgoing links from DBpedia
            Value outDegree = bindings.getValue("outDegree");
            if (outDegree != null) {
                poi.setOutgoingLinks(Integer.parseInt(outDegree.stringValue()));
            } else {
                poi.setOutgoingLinks(0);
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

            pois.add(poi);
        });

    return pois;
}

private String getStringValueOrNull(BindingSet bindings, String name) {
    Value value = bindings.getValue(name);
    return value != null ? value.stringValue() : null;
}
}