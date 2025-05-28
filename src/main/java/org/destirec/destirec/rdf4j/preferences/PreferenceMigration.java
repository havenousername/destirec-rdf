package org.destirec.destirec.rdf4j.preferences;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.PreferenceNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.destirec.destirec.utils.rdfDictionary.UserNames;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Component;

@Component
@Getter
public class PreferenceMigration extends IriMigration implements OntologyDefiner {
    private final DestiRecOntology destiRecOntology;
    public PreferenceMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology destiRecOntology) {
        super(rdf4jMethods, PreferenceNames.Classes.PREFERENCE.str());
        this.destiRecOntology = destiRecOntology;
    }

    @PostConstruct
    public void init() {}

    @Override
    public void defineOntology() {
        var ontology = new PreferenceOntology();
        ontology.definePreference();
    }


    class PreferenceOntology {
        private final OWLDataFactory factory = destiRecOntology.getFactory();
        private final OWLClass preference = factory.getOWLClass(PreferenceNames.Classes.PREFERENCE.owlIri());
        private final OWLClass user = factory.getOWLClass(UserNames.Classes.USER.owlIri());

        public void definePreference() {
            OWLClass attributesCollection = factory
                    .getOWLClass(AttributeNames.Classes.ATTRIBUTES_COLLECTION.owlIri());

            OWLObjectPropertyExpression preferenceAuthor = factory
                    .getOWLObjectProperty(PreferenceNames.Properties.PREFERENCE_AUTHOR.stringValue());
            OWLClassExpression hasExactlyOneUserAuthor = factory.getOWLObjectExactCardinality(1, preferenceAuthor, user);

            // define domain and range for preferenceAuthor
            destiRecOntology.addAxiom(factory.getOWLObjectPropertyDomainAxiom(preferenceAuthor, preference));
            destiRecOntology.addAxiom(factory.getOWLObjectPropertyRangeAxiom(preferenceAuthor, user));

            OWLClassExpression properties = factory
                    .getOWLObjectIntersectionOf(attributesCollection, hasExactlyOneUserAuthor);

            // Define preference
            destiRecOntology
                    .addAxiom(factory.getOWLEquivalentClassesAxiom(preference, properties));
        }
    }

    @Override
    public void setup() {
        super.setup();
    }

    @Override
    public void migrate() {
        super.migrate();
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), SKOS.RELATED_MATCH, WIKIDATA.PREFERENCE)
                .add(get(), RDFS.SUBCLASSOF, TopOntologyNames.Classes.CONCEPT.rdfIri());
    }
}
