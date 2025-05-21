package org.destirec.destirec.rdf4j.region;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.months.MonthDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.region.cost.CostDao;
import org.destirec.destirec.rdf4j.region.feature.FeatureDao;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.InsertDataQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.spring.dao.support.opbuilder.TupleQueryEvaluationBuilder;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Getter
@Repository
public class RegionDao extends GenericDao<RegionConfig.Fields, RegionDto> {
    private final CostDao costDao;
    private final FeatureDao featureDao;

    private final MonthDao monthDao;

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

                connection.begin();
                connection.prepareUpdate(queryString1).execute();
                connection.prepareUpdate(queryString).execute();
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
        ontology.triggerInference();
        return id;
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
        return this.getReadQueryOrUseCached().evaluateAndConvert().toList(this::mapSolution, this::postProcessMappedSolution);
    }

    private TupleQueryEvaluationBuilder getReadQueryOrUseCached() {
        return this.getRdf4JTemplate().tupleQuery(
                this.getClass(),
                "readQuery",
                () -> getReadQuery(Rdf.iri((RegionNames.Classes.LEAF_REGION.rdfIri()))));
    }
}
