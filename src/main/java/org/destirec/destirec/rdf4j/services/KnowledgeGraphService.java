package org.destirec.destirec.rdf4j.services;

import org.destirec.destirec.rdf4j.region.RegionService;
import org.destirec.destirec.rdf4j.region.apiDto.SimpleRegionDto;
import org.destirec.destirec.rdf4j.version.VersionDao;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA;
import org.destirec.destirec.utils.rdfDictionary.RegionNames.Individuals.RegionTypes;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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


    private List<Map<String, Object>> getQueryHandler(
            List<Map.Entry<RegionTypes, Function<String, String>>> regionsHierarchy,
            RegionTypes regionType,
            Optional<Value> parent
    ) {
        return rdf4JTemplate.applyToConnection(connection -> {
            var parentValue = parent.map(Value::stringValue).orElse(null);
            String query = queries.get(regionType).apply(parentValue);
            TupleQuery tupleQuery = connection.prepareTupleQuery(query);
            List<Map<String, Object>> results = new ArrayList<>();
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                result.forEach(value -> {
                    Map<String, Object> row = new HashMap<>();

                    SimpleRegionDto region = new SimpleRegionDto();
                    IRI parentIri = parentValue != null ? factory.createIRI(parentValue) : null;
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
                        row.put(binding.getName(),
                                binding.getValue().stringValue());
                    }
                    IRI id = regionService.createRegion(region, true);
                    row.put("iri", id.stringValue());
                    if (!regionsHierarchy.isEmpty()) {
                        List<Map.Entry<RegionTypes, Function<String, String>>> remainingRegions =
                                regionsHierarchy.subList(1, regionsHierarchy.size());

                        if (!remainingRegions.isEmpty()) {
                            RegionTypes nextRegionType = remainingRegions.getFirst().getKey();
                            assert entity != null;
                            var children = getQueryHandler(
                                    remainingRegions,
                                    nextRegionType,
                                    Optional.of(entity)
                            );
                            row.put("children", children);
                        }

                    }
                    results.add(row);
                });
            }
            return results;
        });
    }

    public List<Map<String, Object>> getAllRegionEntities() {
        Iterator<Map.Entry<RegionTypes, Function<String, String>>> queriesOrder
                = queries.entrySet().iterator();
        return getQueryHandler(queries.entrySet().stream().toList(),  RegionTypes.WORLD, Optional.empty());
    }

    public void addAllRegionsToRepository() {
        if (versionDao.hasRegionVersion()) {
            logger.info("Repository already has all the regions");
            return;
        }

        getQueryHandler(
                queries.entrySet().stream().toList(),
                RegionTypes.WORLD,
                Optional.of(factory.createIRI("http://www.wikidata.org/entity/Q2")));
        logger.info("Repository already has all the regions");
        versionDao.saveRegionVersion(factory.createIRI(WIKIDATA.SPARQL_ENDPOINT.toString()));
    }
}
