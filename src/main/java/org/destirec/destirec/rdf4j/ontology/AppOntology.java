package org.destirec.destirec.rdf4j.ontology;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public interface AppOntology {
    void loadIntoOntology(String iri) throws OWLOntologyCreationException;

    void resetOntology();

    void migrate();

    void triggerInference();
}
