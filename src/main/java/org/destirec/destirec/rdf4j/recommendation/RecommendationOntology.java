package org.destirec.destirec.rdf4j.recommendation;

import org.destirec.destirec.rdf4j.ontology.AppOntology;
import org.semanticweb.owlapi.model.OWLDataFactory;

public class RecommendationOntology {
    private final AppOntology ontology;
    private final OWLDataFactory factory;

    public RecommendationOntology(AppOntology ontology, OWLDataFactory factory) {
        this.ontology = ontology;
        this.factory = factory;
    }


    public void defineUserForRecommendation() {

    }
}
