package org.destirec.destirec.rdf4j.ontology;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;

public interface AppOntology {
    void loadIntoOntology(String iri) throws OWLOntologyCreationException;

    void resetOntology();

    void migrate(OntologyFeature feature);
    void migrate();

    void triggerInference();

    ChangeApplied addAxiom(OWLAxiom axiom, OntologyFeature featureName);

    ChangeApplied addAxiom(OWLAxiom axiom);

    void resetOntologyFeature(OntologyFeature feature);

}
