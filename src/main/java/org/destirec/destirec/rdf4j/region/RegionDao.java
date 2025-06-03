package org.destirec.destirec.rdf4j.region;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.interfaces.daoVisitors.QueryStringVisitor;
import org.destirec.destirec.rdf4j.months.MonthDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.poi.POIDao;
import org.destirec.destirec.rdf4j.poi.POIDto;
import org.destirec.destirec.rdf4j.region.apiDto.RegionDtoWithChildren;
import org.destirec.destirec.rdf4j.region.cost.CostDao;
import org.destirec.destirec.rdf4j.region.feature.FeatureDao;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.utils.SimpleDtoTransformations;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.POINames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames.Individuals.RegionTypes;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Projectable;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.InsertDataQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfResource;
import org.eclipse.rdf4j.spring.dao.support.opbuilder.TupleQueryEvaluationBuilder;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.eclipse.rdf4j.spring.util.QueryResultUtils;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static org.destirec.destirec.utils.Constants.MAX_RDF_RECURSION;

@Getter
@Repository
public class RegionDao extends GenericDao<RegionConfig.Fields, RegionDto> {
    private final CostDao costDao;
    private final FeatureDao featureDao;
    private final MonthDao monthDao;
    private final POIDao pOIDao;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public RegionDao(
            RDF4JTemplate rdf4JTemplate,
            RegionConfig configFields,
            RegionMigration migration,
            RegionDtoCreator dtoCreator,
            CostDao costDao,
            FeatureDao featureDao,
            MonthDao monthDao,
            DestiRecOntology ontology,
            POIDao pOIDao) {
        super(rdf4JTemplate, configFields, migration, dtoCreator, ontology);
        this.costDao = costDao;
        this.featureDao = featureDao;
        this.monthDao = monthDao;
        this.pOIDao = pOIDao;
    }


    @Override
    public String getReadQuery() {
        return super.getReadQuery();
    }

    @Override
    public IRI saveAndReturnId(RegionDto dto, IRI iri) {
        IRI id = super.saveAndReturnId(dto, iri);
        getRdf4JTemplate().applyToConnection(connection -> {
            if (dto.getParentRegion() != null) {
                Variable obj = SparqlBuilder.var("obj");
                GraphPatternNotTriples wherePattern = GraphPatterns.and(
                        GraphPatterns.tp(dto.getParentRegion(), RegionNames.Properties.CONTAINS_EMPTY.rdfIri(), obj)
                );
                TriplePattern deletePattern = GraphPatterns.tp(dto.getParentRegion(), RegionNames.Properties.CONTAINS_EMPTY.rdfIri(), obj);
                ModifyQuery deleteQuery = Queries.DELETE()
                        .with(TopOntologyNames.Graph.INFERRED.rdfIri())
                        .delete(deletePattern)
                        .where(wherePattern);

                ModifyQuery deleteQueryDefault = Queries.DELETE()
                        .delete(deletePattern)
                        .where(wherePattern);

                // Get the SPARQL query string for logging
                String queryString = deleteQuery.getQueryString();
                String queryString1 = deleteQueryDefault.getQueryString();

                Variable hasFeatureQuality = SparqlBuilder.var("hasFeatureQuality");
                Variable quality = SparqlBuilder.var("quality");
                // remove also all the quality classifications for region
                GraphPatternNotTriples whereQuality = GraphPatterns.and(
                        GraphPatterns.tp(dto.getParentRegion(), RDF.TYPE, RegionNames.Classes.REGION.rdfIri()),
                        GraphPatterns.tp(dto.getParentRegion(), hasFeatureQuality, quality),
                        GraphPatterns.tp(hasFeatureQuality, RDFS.SUBPROPERTYOF, AttributeNames.Properties.HAS_QUALITY.rdfIri())
                );

                TriplePattern deleteQualityPattern = GraphPatterns.
                        tp(dto.getParentRegion(), hasFeatureQuality, quality);

                ModifyQuery deleteQuality = Queries.DELETE()
                        .delete(deleteQualityPattern)
                        .where(whereQuality);

                connection.begin();
                connection.prepareUpdate(queryString1).execute();
                connection.prepareUpdate(queryString).execute();
                String deleteQueryQuality = deleteQuality.getQueryString();
                connection.prepareUpdate(deleteQueryQuality);
                connection.commit();
            }

            TriplePattern tripleChildContains = GraphPatterns.tp(
                    iri,
                    RegionNames.Properties.CONTAINS_EMPTY.rdfIri(),
                    RegionNames.Individuals.NO_REGION.rdfIri());
            InsertDataQuery insertQuery = Queries.INSERT_DATA(tripleChildContains);
            String queryString = insertQuery.getQueryString();

            connection.begin();
            connection.prepareUpdate(queryString).execute();
            connection.commit();
            return queryString;
        });
        return id;
    }

    public Optional<IRI> getByType(RegionTypes regionType) {
        return Optional.of(listByTypeId(regionType).stream().findFirst().map(Pair::getValue0)).orElse(null);
    }


    public IRI getBySource(IRI source) {
        Variable regionId = SparqlBuilder.var("regionId");
        SelectQuery query = Queries.SELECT(regionId);
        query.where(
                regionId.has(DC.SOURCE, source)
        ).limit(1);

        return getRdf4JTemplate().applyToConnection(connection -> {
            try {
                TupleQueryResult res = connection.prepareTupleQuery(query.getQueryString()).evaluate();
                if (res.hasNext()) {
                    BindingSet binding = res.next();
                    return (IRI)binding.getValue(regionId.getVarName());
                }
            } catch (Exception e) {
                logger.error("Cannot find region version");
            }
            return null;
        });
    }

    @Override
    public RegionConfig getConfigFields() {
        return (RegionConfig) super.getConfigFields();
    }

    @Override
    public RegionDtoCreator getDtoCreator() {
        return (RegionDtoCreator) super.getDtoCreator();
    }

    public List<RegionDto> listLeaf() {
        return this.getReadQueryOrUseCached()
                .evaluateAndConvert()
                .toList(this::mapSolution, this::postProcessMappedSolution);
    }

    private TupleQueryEvaluationBuilder getReadQueryOrUseCached() {
        return this.getRdf4JTemplate().tupleQuery(
                this.getClass(),
                "readQuery",
                () -> getReadQuery(Rdf.iri((RegionNames.Classes.LEAF_REGION.rdfIri()))));
    }

    public List<Pair<IRI, IRI>> listByTypeId(RegionTypes regionType) {
        return this.getReadQueryListByType(regionType)
                .evaluateAndConvert()
                .toList(solution -> new Pair<>(
                        valueFactory.createIRI(solution.getValue("regionId").stringValue()),
                        valueFactory.createIRI(solution.getValue("sourceId").stringValue())))
                ;
    }

    public List<Pair<IRI, IRI>> listByTypeIdWithChild(RegionTypes regionType) {
        return this.getReadQueryListByTypeNoChild(regionType)
                .evaluateAndConvert()
                .toList(solution -> new Pair<>(
                        valueFactory.createIRI(solution.getValue("regionId").stringValue()),
                        valueFactory.createIRI(solution.getValue("sourceId").stringValue())))
                ;
    }

    private TupleQueryEvaluationBuilder getAllWithChildren(IRI regionId) {
        return this.getRdf4JTemplate()
                .tupleQuery(getClass(), "KEY_BY_ID_WITH_CHILDREN", () -> getByIdAllChildrenQuery(migration.getResource(), regionId));
    }

    public RegionDtoWithChildren getByIdWithChildren(IRI regionId) {
        return this.getAllWithChildren(regionId)
                .evaluateAndConvert()
                .toSingleton(this::mapSolutionWithChildren, r -> r);
    }

    public RegionDtoWithChildren mapSolutionWithChildren(BindingSet querySolution) {
        if (mapEnteredTimes.getAndIncrement() >= MAX_RDF_RECURSION) {
            throw new IllegalCallerException("Reached maximum depth of " + MAX_RDF_RECURSION + ", exiting application.");
        }

        var poisVar = SparqlBuilder.var("pois");
        var childrenVar = SparqlBuilder.var("children");
        var featuresVar = SparqlBuilder.var("features");

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

        String regionsConcat = querySolution.getValue(childrenVar.getVarName()).stringValue();
        String poisConcat = querySolution.getValue(poisVar.getVarName()).stringValue();
        String featuresConcat = querySolution.getValue(featuresVar.getVarName()).stringValue();

        List<String> regionsString = Optional.ofNullable(regionsConcat)
                .map(SimpleDtoTransformations::toListString)
                .orElse(Collections.emptyList());

        List<String> poisString = Optional.ofNullable(poisConcat)
                .map(SimpleDtoTransformations::toListString)
                .orElse(Collections.emptyList());

        List<String> featuresString = Optional.ofNullable(featuresConcat)
                .map(SimpleDtoTransformations::toListString)
                .orElse(Collections.emptyList());

        List<RegionDto> regions =regionsString
                .stream()
                .filter(region -> !region.isBlank())
                .map(poi -> getById(valueFactory.createIRI(poi)))
                .toList();

        List<POIDto> pois = poisString
                .stream()
                .filter(region -> !region.isBlank())
                .map(poi -> pOIDao.getById(valueFactory.createIRI(poi)))
                .toList();

        List<FeatureDto> features = featuresString
                .stream()
                .filter(region -> !region.isBlank())
                .map(feature -> featureDao.getById(valueFactory.createIRI(feature)))
                .toList();

        RegionDto dto = getDtoCreator().create(id, map);

        return new RegionDtoWithChildren(dto, regions, pois, features);
    }

    protected String getByIdAllChildrenQuery(RdfResource graph, IRI regionId) {
        var queryParams = getSelectParams(graph);
        var whereParams = new ArrayList<>(List.of(queryParams.getValue1()));

        var targetRegionVar = configFields.getId();
        var childRegionVar = SparqlBuilder.var("childRegion");
        var regionsPoiVar = SparqlBuilder.var("regionsPoi");
        var featuresFromPoiVar = SparqlBuilder.var("featuresFromPoi");

        // AGGREGATED Variables
        var pois = SparqlBuilder.var("pois");
        var childrenVar = SparqlBuilder.var("children");
        var features = SparqlBuilder.var("features");

        GraphPattern assignRegion = GraphPatterns.and().values(builder-> {
            builder.variables(targetRegionVar);
            builder.value(Rdf.iri(regionId));
        });

        TriplePattern getChildren = GraphPatterns.tp(targetRegionVar,
                valueFactory.createIRI(RegionNames.Properties.SF_D_CONTAINS), childRegionVar);
        TriplePattern getChildrenRegions = GraphPatterns.tp(childRegionVar,
                RDF.TYPE, RegionNames.Classes.REGION.rdfIri());
        TriplePattern isPoi = GraphPatterns.tp(regionsPoiVar, RDF.TYPE, POINames.Classes.POI.rdfIri());
        TriplePattern getRegionsPoi = GraphPatterns.tp(targetRegionVar,
                valueFactory.createIRI(RegionNames.Properties.SF_CONTAINS), regionsPoiVar);
        TriplePattern getChildContainsPoi = GraphPatterns.tp(childRegionVar,
                valueFactory.createIRI(RegionNames.Properties.SF_CONTAINS), regionsPoiVar);
        TriplePattern getFeaturesFromPoi = GraphPatterns.tp(regionsPoiVar,
                AttributeNames.Properties.HAS_FEATURE.rdfIri(), featuresFromPoiVar);

        List<Projectable> selectParams = new ArrayList<>(
                queryParams.getValue0()
                        .stream()
                        .map(Map.Entry::getValue)
                        .toList()
        );

        selectParams.add(Expressions.group_concat("\",\"", childRegionVar).as(childrenVar));
        selectParams.add(Expressions.group_concat("\",\"", regionsPoiVar).as(pois));
        selectParams.add(Expressions.group_concat("\",\"", featuresFromPoiVar).as(features));
//        selectParams = selectParams.stream().filter(proj -> !proj.getQueryString().contains("_FEATURES")).toList();

        whereParams.addFirst(assignRegion);

        GraphPatternNotTriples subQueryForRegion = GraphPatterns.and(
                getChildren,
                getChildrenRegions,
                getChildContainsPoi,
                isPoi,
                getRegionsPoi,
                getFeaturesFromPoi
        );

        GraphPatternNotTriples subQueryForDistricts = GraphPatterns.and(
                GraphPatterns.filterNotExists(
                        getChildren,
                        getChildrenRegions
                ),
                GraphPatterns.tp(targetRegionVar,  valueFactory.createIRI(RegionNames.Properties.SF_CONTAINS), regionsPoiVar),
                isPoi,
                getFeaturesFromPoi
        );

        whereParams
                .add(GraphPatterns.union(subQueryForRegion, subQueryForDistricts));
        String query = Queries.SELECT(selectParams
                        .toArray(Projectable[]::new))
                .where(whereParams.toArray(GraphPattern[]::new))
                .groupBy(queryParams.getValue2())
                .getQueryString();
        return query;
    }

    public List<RegionDto> listAllByType(RegionTypes regionType, int page, int pageSize) {
        return this.getRdf4JTemplate()
                .tupleQuery(getClass(), "KEY_LIST_ALL_BY_TYPE", () ->
                        getListAllByType(migration.getResource(), regionType, page, pageSize))
                .evaluateAndConvert()
                .toList(this::mapSolution, this::postProcessMappedSolution);
    }

    public List<RegionDto> listAllByType(RegionTypes regionType) {
        return this.getRdf4JTemplate()
                .tupleQuery(getClass(), "KEY_LIST_ALL_BY_TYPE", () ->
                        getListAllByType(migration.getResource(), regionType))
                .evaluateAndConvert()
                .toList(this::mapSolution, this::postProcessMappedSolution);
    }

    public List<IRI> listAllCountriesForRegion(IRI regionId) {
        return this.getRdf4JTemplate()
                .tupleQuery(getClass(), "LIST_ALL_COUNTRIES_FOR_ID", () ->
                        getAllCountriesOfParent(regionId))
                .evaluateAndConvert()
                .toList(solution -> valueFactory.createIRI(solution.getValue("children").stringValue()));
    }

    public String getAllCountriesOfParent(IRI regionId) {
        Variable regionIdVar = SparqlBuilder.var("regionId");
        Variable childrenVar = SparqlBuilder.var("children");
        TriplePattern selectRegion = GraphPatterns.tp(regionIdVar, RDF.TYPE, Rdf.iri(RegionNames.Classes.REGION.rdfIri()));
        TriplePattern selectChildren = GraphPatterns.tp(
                regionId,
                valueFactory.createIRI(RegionNames.Properties.SF_CONTAINS) ,
                childrenVar);
        TriplePattern childrenAreCountries = GraphPatterns.tp(childrenVar, RegionNames.Properties.HAS_LEVEL.rdfIri(), RegionTypes.COUNTRY.iri().rdfIri());

        GraphPattern variableRegionBind = GraphPatterns.and().values((builder) -> {
            builder.variables(regionIdVar);
            builder.value(Rdf.iri(regionId));
        });
        return Queries.SELECT(childrenVar).where(variableRegionBind, selectRegion, selectChildren, childrenAreCountries).getQueryString();
    }

    @Override
    protected RegionDto mapSolution(BindingSet bindingSet) {
        return super.mapSolution(bindingSet);
    }

    private TupleQueryEvaluationBuilder getReadQueryListByType(RegionTypes regionType) {
        return this.getRdf4JTemplate()
                .tupleQuery(getClass(), "KEY_LIST_ALL_BY_TYPE", () -> getListAllByTypeQueryId(regionType));
    }

    private TupleQueryEvaluationBuilder getReadQueryListByTypeNoChild(RegionTypes regionType) {
        return this.getRdf4JTemplate()
                .tupleQuery(getClass(), "KEY_LIST_ALL_BY_TYPE_NO_CHILD", () -> getListAllByTypeQueryIdWithNoChild(regionType));
    }

    protected String getListAllByType(RdfResource graph, RegionTypes regionType) {
        var queryParams = getSelectParams(graph);
        var whereParams = new ArrayList<>(List.of(queryParams.getValue1()));
        whereParams.add(GraphPatterns.tp(configFields.getId(), RegionNames.Properties.HAS_LEVEL.rdfIri(), regionType.iri().rdfIri()));
        return Queries.SELECT(queryParams.getValue0()
                        .stream()
                        .map(Map.Entry::getValue)
                        .toArray(Projectable[]::new))
                .distinct()
                .where(whereParams.toArray(GraphPattern[]::new))
                .groupBy(queryParams.getValue2())
                .getQueryString();
    }


    protected String getListAllByType(RdfResource graph, RegionTypes regionType, int page, int pageSize) {
        var queryParams = getSelectParams(graph);
        var whereParams = new ArrayList<>(List.of(queryParams.getValue1()));
        whereParams.add(GraphPatterns.tp(configFields.getId(), RegionNames.Properties.HAS_LEVEL.rdfIri(), regionType.iri().rdfIri()));
        return Queries.SELECT(queryParams.getValue0()
                        .stream()
                        .map(Map.Entry::getValue)
                        .toArray(Projectable[]::new))
                .distinct()
                .where(whereParams.toArray(GraphPattern[]::new))
                .groupBy(queryParams.getValue2())
                .limit(pageSize)
                .offset(page * pageSize)
                .getQueryString();
    }

    public long getTotalCountByType(RegionTypes type) {
        String countQuery = getCountryByType(migration.getResource(), type);
        var countResult = getRdf4JTemplate()
                .tupleQuery(getClass(), "KEY_COUNT_BY_TYPE_QUERY", () -> countQuery)
                .evaluateAndConvert()
                .toStream()
                .findFirst();

        return countResult.map(bindings -> Long.parseLong(bindings.getBinding("count")
                .getValue().stringValue())).orElse(0L);

    }

    protected String getCountryByType(RdfResource graph, RegionTypes regionType) {
        return Queries.SELECT(Expressions.countAll().as(SparqlBuilder.var("count")))
                .where(configFields.getId().isA(graph),
                        GraphPatterns.tp(configFields.getId(), RegionNames.Properties.HAS_LEVEL.rdfIri(), regionType.iri().rdfIri()))
                .getQueryString();
    }

    protected String getListAllByTypeQueryIdWithNoChild(RegionTypes regionType) {
        Variable regionId = SparqlBuilder.var("regionId");
        Variable sourceId = SparqlBuilder.var("sourceId");
        TriplePattern selectRegion = GraphPatterns.tp(regionId, RDF.TYPE, Rdf.iri(RegionNames.Classes.REGION.rdfIri()));
        TriplePattern selectLeaf = GraphPatterns.tp(
                regionId,
                RegionNames.Properties.HAS_LEVEL.rdfIri(),
                regionType.iri().rdfIri());

        TriplePattern source = GraphPatterns.tp(regionId, DC.SOURCE, sourceId);
        Variable childId = SparqlBuilder.var("childId");
        TriplePattern hasAChild = GraphPatterns.tp(regionId, valueFactory.createIRI(RegionNames.Properties.SF_D_CONTAINS), childId);
        GraphPattern filter = GraphPatterns.filterNotExists(hasAChild);
        return Queries.SELECT(regionId, sourceId).distinct().where(selectRegion, selectLeaf, source, filter).getQueryString();
    }

    protected String getListAllByTypeQueryId(RegionTypes regionType) {
        Variable regionId = SparqlBuilder.var("regionId");
        Variable sourceId = SparqlBuilder.var("sourceId");
        TriplePattern selectRegion = GraphPatterns.tp(regionId, RDF.TYPE, Rdf.iri(RegionNames.Classes.REGION.rdfIri()));
        TriplePattern selectLeaf = GraphPatterns.tp(
                regionId,
                RegionNames.Properties.HAS_LEVEL.rdfIri(),
                regionType.iri().rdfIri());

        TriplePattern source = GraphPatterns.tp(regionId, DC.SOURCE, sourceId);
        return Queries.SELECT(regionId, sourceId).distinct().where(selectRegion, selectLeaf, source).getQueryString();
    }

    public List<Triplet<IRI, String, String>> getCountryScores(IRI regionId) {
        return this.getRdf4JTemplate()
                .tupleQuery(getClass(), "GET_COUNTRY_FEATURE_SCORES", () ->
                        getCountryScoresQuery(regionId))
                .evaluateAndConvert()
                .toList(solution -> {
                    IRI region = valueFactory.createIRI(solution.getValue("regionId").stringValue());
                    String scores = solution.getValue("scores").stringValue();
                    return Triplet.with(region, scores, solution.getValue("featureName").stringValue());
                });
    }

    protected String getCountryScoresQuery(IRI regionId) {
        Variable regionIdVar = SparqlBuilder.var("regionId");
        Variable scoreVar = SparqlBuilder.var("score");
        Variable qualityVar = SparqlBuilder.var("quality");
        Variable hasFeatureQualityVar = SparqlBuilder.var("hasFeatureQuality");
        Variable featureVar = SparqlBuilder.var("feature");
        Variable scoresVar = SparqlBuilder.var("scores");
        Variable featureNameVar = SparqlBuilder.var("featureName");
        Variable poiVar = SparqlBuilder.var("poi");

        GraphPattern valueOfRegion = GraphPatterns.and().values((builder) -> {
            builder.variables(regionIdVar);
            builder.value(Rdf.iri(regionId));
        });
        TriplePattern selectRegion = GraphPatterns.tp(regionIdVar, RDF.TYPE, RegionNames.Classes.REGION.rdfIri());
        TriplePattern selectFeatureQuality = GraphPatterns.tp(regionIdVar, hasFeatureQualityVar, qualityVar);
        TriplePattern selectHasQuality = GraphPatterns.tp(hasFeatureQualityVar, RDFS.SUBPROPERTYOF, AttributeNames.Properties.HAS_QUALITY.rdfIri());
        TriplePattern selectFeature = GraphPatterns.tp(hasFeatureQualityVar, RegionNames.Properties.FOR_FEATURE.rdfIri(), featureVar);
        TriplePattern selectScore = GraphPatterns.tp(featureVar, AttributeNames.Properties.HAS_SCORE.rdfIri(), scoreVar);
        TriplePattern featureName = GraphPatterns.tp(featureVar, AttributeNames.Properties.HAS_REGION_FEATURE.rdfIri(), featureNameVar);
        TriplePattern poiHasFeature = GraphPatterns.tp(poiVar, AttributeNames.Properties.HAS_FEATURE.rdfIri(), featureVar);

        String query = Queries.SELECT(regionIdVar, featureNameVar, Expressions.group_concat("\",\"", scoreVar).as(scoresVar))
                .where(
                        valueOfRegion,
                        selectRegion,
                        selectFeatureQuality,
                        selectHasQuality,
                        selectFeature,
                        selectScore,
                        featureName,
                        poiHasFeature
                )
                .groupBy(regionIdVar, featureNameVar)
                .getQueryString();
        return query;
    }

}


