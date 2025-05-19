package org.destirec.destirec.rdf4j.region;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.months.MonthDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.region.cost.CostDao;
import org.destirec.destirec.rdf4j.region.feature.FeatureDao;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
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
                var dContainsIRI = valueFactory.createIRI(RegionNames.Properties.SF_D_CONTAINS);
                var tContainsIRI = valueFactory.createIRI(RegionNames.Properties.SF_CONTAINS);
                var dWithinIRI = valueFactory.createIRI(RegionNames.Properties.SF_D_WITHIN);
                var tWithinIRI = valueFactory.createIRI(RegionNames.Properties.SF_WITHIN);
                Variable region = SparqlBuilder.var("region");
                GraphPatternNotTriples wherePattern = GraphPatterns.and(
                        GraphPatterns.tp(dto.getParentRegion(), dContainsIRI, region),
                        GraphPatterns.tp(dto.getParentRegion(), tContainsIRI, region),
                        GraphPatterns.tp(region, RDF.TYPE,  RegionNames.Classes.NO_REGION.rdfIri()),
                        GraphPatterns.tp(region, dWithinIRI, dto.getParentRegion())
                );

                ModifyQuery deleteQuery = Queries.DELETE()
                        .delete(
                                GraphPatterns.tp(dto.getParentRegion(), dContainsIRI, region),
                                GraphPatterns.tp(dto.getParentRegion(), tContainsIRI, region),
                                GraphPatterns.tp(region, dWithinIRI, dto.getParentRegion()),
                                GraphPatterns.tp(region, tWithinIRI, dto.getParentRegion())
                        )
                        .where(wherePattern);

                // Get the SPARQL query string for logging
                String queryString = deleteQuery.getQueryString();
                connection.begin();
                connection.prepareUpdate(queryString).execute();
                connection.commit();
            }
            String query = "INSERT DATA { <" +
                    RegionNames.Individuals.NO_REGION.pseudoUri() +
                    "> <" +
                    RegionNames.Properties.SF_D_WITHIN +
                    "> <" +
                    iri.stringValue() +
                    "> }";

            connection.begin();
            connection.prepareUpdate(query).execute();
            connection.commit();
            return query;
        });
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
