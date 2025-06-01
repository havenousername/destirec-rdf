package org.destirec.destirec.rdf4j.region;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.months.MonthDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.region.cost.CostDao;
import org.destirec.destirec.rdf4j.region.feature.FeatureDao;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
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
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Repository
public class RegionDao extends GenericDao<RegionConfig.Fields, RegionDto> {
    private final CostDao costDao;
    private final FeatureDao featureDao;
    private final MonthDao monthDao;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public RegionDao(
            RDF4JTemplate rdf4JTemplate,
            RegionConfig configFields,
            RegionMigration migration,
            RegionDtoCreator dtoCreator,
            CostDao costDao,
            FeatureDao featureDao,
            MonthDao monthDao,
            DestiRecOntology ontology
    ) {
        super(rdf4JTemplate, configFields, migration, dtoCreator, ontology);
        this.costDao = costDao;
        this.featureDao = featureDao;
        this.monthDao = monthDao;
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
                {
                    return getAllCountriesOfParent(regionId);
                })
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


    protected String getListAllByTypeQueryId(RegionTypes regionType) {
        Variable regionId = SparqlBuilder.var("regionId");
        Variable sourceId = SparqlBuilder.var("sourceId");
        TriplePattern selectRegion = GraphPatterns.tp(regionId, RDF.TYPE, Rdf.iri(RegionNames.Classes.REGION.rdfIri()));
        TriplePattern selectLeaf = GraphPatterns.tp(
                regionId,
                RegionNames.Properties.HAS_LEVEL.rdfIri(),
                regionType.iri().rdfIri());

        TriplePattern source = GraphPatterns.tp(regionId, DC.SOURCE, sourceId);
        return Queries.SELECT(regionId, sourceId).where(selectRegion, selectLeaf, source).getQueryString();
    }
}
