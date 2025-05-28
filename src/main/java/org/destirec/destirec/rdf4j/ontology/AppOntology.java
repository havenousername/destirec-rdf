package org.destirec.destirec.rdf4j.ontology;

import org.eclipse.rdf4j.model.Resource;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;

public interface AppOntology {
    void loadIntoOntology(String iri) throws OWLOntologyCreationException;

    void resetOntology();

    void migrate(String feature);
    void migrate();

    void triggerInference();

    OWLDataFactory getFactory();

    ChangeApplied addAxiom(OWLAxiom axiom, OntologyFeature featureName);

    ChangeApplied addAxiom(OWLAxiom axiom, String featureName);

    ChangeApplied addAxiom(OWLAxiom axiom);

    ChangeApplied removeAxiom(OWLAxiom axiom);

    ChangeApplied removeAxiom(OWLAxiom axiom, OntologyFeature featureName);

    ChangeApplied removeAxiom(OWLAxiom axiom, String featureName);

    void resetOntologyFeature(OntologyFeature feature);

    void resetOntologyFeature(String feature);

    ChangeApplied removeDatabaseAxioms(String featureName, Resource subject);
}
