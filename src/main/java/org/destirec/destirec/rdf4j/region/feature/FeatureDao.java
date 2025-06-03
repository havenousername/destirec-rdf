package org.destirec.destirec.rdf4j.region.feature;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.eclipse.rdf4j.model.IRI;
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

    public void removeByHasFeatureConnection(IRI author) {
        var features = getByHasFeatureConnection(author);
        for (IRI feature : features) {
            this.delete(feature);
        }
    }

    public List<IRI> getByHasFeatureConnection(IRI author) {
        String query = getByHasFeatureConnectionQuery(author);
        var result = getRdf4JTemplate()
                .tupleQuery(getClass(), "KEY_BY_FEATURE_AUTHOR", () -> query)
                .evaluateAndConvert()
                .toStream()
                .toList();

        return result.stream().map(bindings ->
                        (IRI) bindings.getBinding("featureId").getValue())
                .toList();
    }


    protected String getByHasFeatureConnectionQuery(IRI author) {
        Variable featureId = SparqlBuilder.var("featureId");
        TriplePattern featureType = GraphPatterns.tp(featureId, RDF.TYPE, AttributeNames.Classes.FEATURE.rdfIri());
        TriplePattern featureAuthor = GraphPatterns.tp(featureId, AttributeNames.Properties.HAS_FEATURE.rdfIri(), author);
        return Queries.SELECT(featureId)
                .where(featureType, featureAuthor)
                .getQueryString();
    }
}
