package org.destirec.destirec.rdf4j.recommendation;

import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.RecommendationNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;

public class RecommendationMigration extends IriMigration implements OntologyDefiner {
    private final DestiRecOntology destiRecOntology;
    public RecommendationMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology destiRecOntology) {
        super(rdf4jMethods, RecommendationNames.Classes.RECOMMENDATION.str());
        this.destiRecOntology = destiRecOntology;
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBPROPERTYOF, TopOntologyNames.Classes.OBJECT.rdfIri());
    }

    @Override
    public void defineOntology() {
    }

}
