package org.destirec.destirec.rdf4j.region.feature;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FeatureDao extends GenericDao<FeatureConfig.Fields, FeatureDto> {
    public FeatureDao(
            RDF4JTemplate rdf4JTemplate,
            FeatureConfig configFields,
            FeatureMigration migration,
            FeatureDtoCreator dtoCreator,
            DestiRecOntology ontology
    ) {
        super(rdf4JTemplate, configFields, migration, dtoCreator, ontology);
    }

    @Override
    public FeatureDtoCreator getDtoCreator() {
        return (FeatureDtoCreator) super.getDtoCreator();
    }

    public void removeByHasFeatureConnection(IRI parent) {
        var features = getByHasFeatureConnection(parent);
        List<Statement> statements = features.stream().flatMap(i -> createTriples(getById(i), i).stream()).toList();
        getRdf4JTemplate().consumeConnection(connection -> connection.remove(statements));
    }

    public List<IRI> getByHasFeatureConnection(IRI parent) {
        String query = getByHasFeatureConnectionQuery(parent);
        var result = getRdf4JTemplate()
                .tupleQuery(getClass(), "KEY_BY_FEATURE_PARENT", () -> query)
                .evaluateAndConvert()
                .toStream()
                .toList();

        return result.stream().map(bindings ->
                        (IRI) bindings.getBinding("featureId").getValue())
                .toList();
    }


    protected String getByHasFeatureConnectionQuery(IRI parent) {
        Variable featureId = SparqlBuilder.var("featureId");
        TriplePattern featureType = GraphPatterns.tp(featureId, RDF.TYPE, AttributeNames.Classes.FEATURE.rdfIri());
        TriplePattern featureParent = GraphPatterns.tp(parent, AttributeNames.Properties.HAS_FEATURE.rdfIri(), featureId);
        return Queries.SELECT(featureId)
                .where(featureType, featureParent)
                .getQueryString();
    }
}
