package org.destirec.destirec.rdf4j.poi;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.interfaces.daoVisitors.QueryStringVisitor;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.region.feature.FeatureDao;
import org.destirec.destirec.utils.rdfDictionary.POINames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Groupable;
import org.eclipse.rdf4j.sparqlbuilder.core.Projectable;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfResource;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.eclipse.rdf4j.spring.util.QueryResultUtils;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.destirec.destirec.utils.Constants.MAX_RDF_RECURSION;

@Getter
@Repository
public class POIDao extends GenericDao<POIConfig.Fields, POIDto> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private final FeatureDao featureDao;

    public POIDao(
            RDF4JTemplate rdf4JTemplate,
            POIConfig configFields,
            POIMigration migration,
            POIDtoCreator dtoCreator,
            FeatureDao featureDao,
            DestiRecOntology ontology
    ) {
        super(rdf4JTemplate, configFields, migration, dtoCreator, ontology);
        this.featureDao = featureDao;
    }


    @Override
    public POIDtoCreator getDtoCreator() {
        return (POIDtoCreator)super.getDtoCreator();
    }

    public List<POIDtoWithHops> listAllByRegion(IRI regionId) {
        return this.getRdf4JTemplate()
                .tupleQuery(getClass(), "KEY_LIST_ALL_BY_REGION", () -> getListAllByType(
                        migration.getResource(),
                                regionId)
                        )
                .evaluateAndConvert()
                .toList(this::mapSolutionWithHops, this::postProcessMappedSolutionWithHops);
    }

    protected POIDtoWithHops postProcessMappedSolutionWithHops(POIDtoWithHops entity) {
        return entity;
    }

    public POIDtoWithHops mapSolutionWithHops(BindingSet querySolution) {
        if (mapEnteredTimes.getAndIncrement() >= MAX_RDF_RECURSION) {
            throw new IllegalCallerException("Reached maximum depth of " + MAX_RDF_RECURSION + ", exiting application.");
        }

        var targetRegion = SparqlBuilder.var("targetRegion");
        var hopCount = SparqlBuilder.var("hopCount");

        IRI id = QueryResultUtils.getIRI(querySolution, configFields.getId());
        var map = configFields.getVariableNames()
                .keySet()
                .stream()
                .map((key) -> {
                    var variableContainer = configFields.getVariable(key);
                    QueryStringVisitor visitor = new QueryStringVisitor(querySolution);
                    variableContainer.accept(visitor);
                    return Map.entry(key, visitor.getQueryString().toString());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String targetValue = querySolution.getValue(targetRegion.getVarName()).stringValue();
        String hopCountValue = querySolution.getValue(hopCount.getVarName()).stringValue();

        POIDto dto = getDtoCreator().create(id, map);
        return getDtoCreator().create(dto, targetValue, hopCountValue);
    }

    protected String getListAllByType(RdfResource graph, IRI regionId) {
        var queryParams = getSelectParams(graph);
        var whereParams = new ArrayList<>(List.of(queryParams.getValue1()));
        var targetRegion = SparqlBuilder.var("targetRegion");
        var inBetweenRegion = SparqlBuilder.var("inBetweenRegion");
        var hopCount = SparqlBuilder.var("hopCount");

        RdfPredicate geoWithinAtLeastOnce = () -> "<" + RegionNames.Properties.SF_D_WITHIN + ">+";
        RdfPredicate geoWithinZeroOrMore = () -> "<" + RegionNames.Properties.SF_D_WITHIN + ">*";



        GraphPattern assignRegion = GraphPatterns.and().values(builder-> {
            builder.variables(targetRegion);
            builder.value(Rdf.iri(regionId));
        });
        TriplePattern getTransitiveRegion = GraphPatterns.tp(configFields.getId(), valueFactory.createIRI(RegionNames.Properties.SF_WITHIN), targetRegion);
        TriplePattern getInBetweenRegion = GraphPatterns.tp(configFields.getId(), geoWithinAtLeastOnce, inBetweenRegion);
        TriplePattern geoFromBetweenToRegion = GraphPatterns.tp(inBetweenRegion, geoWithinZeroOrMore, targetRegion);
        GraphPatternNotTriples filter = GraphPatterns.filterExists(geoFromBetweenToRegion);

        List<Projectable> groupablePatterns = queryParams.getValue0().stream().map(Map.Entry::getValue).collect(Collectors.toList());
        groupablePatterns.add(targetRegion);

        List<Projectable> selectParams = new ArrayList<>(
                queryParams.getValue0()
                        .stream()
                        .map(Map.Entry::getValue)
                        .toList()
        );

        selectParams.add(targetRegion);
        selectParams.add(Expressions.count(inBetweenRegion).as(hopCount));

        whereParams
                .addAll(List.of(
                        assignRegion,
                        getTransitiveRegion,
                        getInBetweenRegion,
                        geoFromBetweenToRegion,
                        filter
                ));
        return Queries.SELECT(selectParams
                        .toArray(Projectable[]::new))
                .where(whereParams.toArray(GraphPattern[]::new))
                .groupBy(groupablePatterns.toArray(new Groupable[0]))
                .getQueryString();
    }


    public List<Triplet<IRI, IRI, Integer>> listAllByRegionOnlyPOI(IRI regionId) {
        return this.getRdf4JTemplate()
                .tupleQuery(getClass(), "LIST_ALL_BY_REGION_ONLY_POI", () -> getListAllByTypeOnlyIRI(migration.getResource(), regionId))
                .evaluateAndConvert()
                .toList(solution -> {
                    IRI target = valueFactory.createIRI(solution.getValue("targetRegion").stringValue());
                    IRI poi = valueFactory.createIRI(solution.getValue(configFields.getId().getVarName()).stringValue());
                    int count = Integer.parseInt(solution.getValue("hopCount").stringValue());

                    return new Triplet<>(poi, target, count);
                });
    }


    protected String getListAllByTypeOnlyIRI(RdfResource graph, IRI regionId) {
        var targetRegion = SparqlBuilder.var("targetRegion");
        var inBetweenRegion = SparqlBuilder.var("inBetweenRegion");
        var hopCount = SparqlBuilder.var("hopCount");

        RdfPredicate geoWithinAtLeastOnce = () -> "<" + RegionNames.Properties.SF_D_WITHIN + ">+";
        RdfPredicate geoWithinZeroOrMore = () -> "<" + RegionNames.Properties.SF_D_WITHIN + ">*";



        TriplePattern isPOI = GraphPatterns.tp(configFields.getId(), RDF.TYPE, POINames.Classes.POI.rdfIri());
        GraphPattern assignRegion = GraphPatterns.and().values(builder-> {
            builder.variables(targetRegion);
            builder.value(Rdf.iri(regionId));
        });
        TriplePattern getTransitiveRegion = GraphPatterns.tp(configFields.getId(), valueFactory.createIRI(RegionNames.Properties.SF_WITHIN), targetRegion);
        TriplePattern getInBetweenRegion = GraphPatterns.tp(configFields.getId(), geoWithinAtLeastOnce, inBetweenRegion);
        TriplePattern geoFromBetweenToRegion = GraphPatterns.tp(inBetweenRegion, geoWithinZeroOrMore, targetRegion);
        GraphPatternNotTriples filter = GraphPatterns.filterExists(geoFromBetweenToRegion);

        String str = Queries.SELECT(configFields.getId(), targetRegion, Expressions.count(inBetweenRegion).as(hopCount))
                .where(isPOI, assignRegion, getTransitiveRegion, getInBetweenRegion, geoFromBetweenToRegion, filter)
                .groupBy(targetRegion, configFields.getId())
                .getQueryString();

        return str;
    }


//    SELECT ?poi ?region
//    {
//  ?poi a :POI .
//            ?poi geo:sfWithin ?region .
//    }
}
