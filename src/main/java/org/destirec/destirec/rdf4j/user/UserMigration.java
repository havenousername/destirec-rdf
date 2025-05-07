package org.destirec.destirec.rdf4j.user;

import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.preferences.PreferenceConfig;
import org.destirec.destirec.rdf4j.preferences.PreferenceMigration;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.destirec.destirec.utils.rdfDictionary.UserNames;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class UserMigration extends IriMigration implements OntologyDefiner {
    private final DestiRecOntology ontology;
    private final UserConfig userConfig;

    private final PreferenceMigration preferenceMigration;

    private final PreferenceConfig preferenceConfig;


    private final IRI userWithPreference = UserNames.Classes.USER_WITH_PREFERENCE.owlIri();

    public UserMigration(
            RDF4JTemplate template,
            DestiRecOntology ontology,
            UserConfig config,
            PreferenceMigration preferenceMigration,
            PreferenceConfig preferenceConfig
    ) {
        super(template, UserNames.Classes.USER.str());
        this.ontology = ontology;
        this.userConfig = config;
        this.preferenceMigration = preferenceMigration;
        this.preferenceConfig = preferenceConfig;
    }

    public void defineOntology() {
        var userOntology = new UserOntology();
        userOntology.defineUserProperties();
        userOntology.defineUserWithPreference();
    }

    class UserOntology {
        OWLClass user = ontology.getFactory().getOWLClass(get().stringValue());


        /**
         * User \sqsubseteq \sqcap (=1\ email).String \ \sqcap  (=1\ userName).String \ \sqcap (=1\ occupation.String)
         */
        public void defineUserProperties() {
            Arrays.stream(UserConfig.Fields.values()).forEach(field -> {
                OWLDataProperty property = ontology.getFactory().getOWLDataProperty(userConfig.getPredicate(field).getItem().stringValue());
                OWLClassExpression propertyRestriction = ontology.getFactory().getOWLDataExactCardinality(1, property);
                OWLSubClassOfAxiom axiom = ontology.getFactory().getOWLSubClassOfAxiom(user, propertyRestriction);
                ontology.getManager().addAxiom(
                        ontology.getOntology(),
                        axiom
                );
            });
        }

        /**
         * UserWithPreference \equiv Preference \sqcap User
         */
        public void defineUserWithPreference() {
            OWLClass userWithPreferenceClass = ontology.getFactory().getOWLClass(userWithPreference);
            OWLClass userPreference = ontology.getFactory().getOWLClass(preferenceMigration.get().stringValue());
            OWLObjectProperty hasAuthor = ontology.getFactory()
                    .getOWLObjectProperty(preferenceConfig
                            .getPredicate(PreferenceConfig.Fields.PREFERENCE_AUTHOR)
                            .getItem().stringValue());
            OWLRestriction preferenceRestriction = ontology.getFactory().getOWLObjectSomeValuesFrom(hasAuthor, user);
            OWLClassExpression expression = ontology.getFactory().getOWLObjectIntersectionOf(userPreference, preferenceRestriction);
            ontology.getManager()
                    .addAxiom(
                            ontology.getOntology(),
                            ontology.getFactory().getOWLEquivalentClassesAxiom(
                                    userWithPreferenceClass,
                                    expression
                            )
                    );
        }
    }

    @Override
    protected void setupProperties() {
        builder.add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, FOAF.PERSON)
                .add(get(), RDFS.SUBCLASSOF, TopOntologyNames.Classes.ACTOR.rdfIri())
                .add(get(), RDFS.COMMENT, "A user of an application");
    }
}
