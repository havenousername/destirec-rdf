package org.destirec.destirec.rdf4j.attribute;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.QualityNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Getter
@Component
public class QualityMigration extends IriMigration implements OntologyDefiner {
    private final DestiRecOntology destirecOntology;
    public QualityMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology ontology) {
        super(rdf4jMethods, QualityNames.Classes.QUALITY.str());
        this.destirecOntology = ontology;
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, TopOntologyNames.Classes.CONCEPT.rdfIri());
    }

    @Override
    public void defineOntology() {
    }

    private class QualityOntology {
        private final OWLDataFactory factory = destirecOntology.getFactory();
        private final OWLOntologyManager manager = destirecOntology.getManager();

        private final OWLOntology ontology = destirecOntology.getOntology();

        private final OWLClass quality = factory.getOWLClass(QualityNames.Classes.QUALITY.owlIri());
        private final OWLClass concept = factory.getOWLClass(TopOntologyNames.Classes.CONCEPT.owlIri());


        public void defineQuality() {
            OWLDataPropertyExpression upperProp = factory.getOWLDataProperty(QualityNames.Properties.UPPER.owlIri());
            OWLDataPropertyExpression lowerProp = factory.getOWLDataProperty(QualityNames.Properties.LOWER.owlIri());

            // Quality ≡ Concept ⊓ (=1 lower) ⊓ (=1 upper)
            OWLClassExpression exactlyOneUpper = factory.getOWLDataExactCardinality(1, upperProp);
            OWLClassExpression exactlyOneLower = factory.getOWLDataExactCardinality(1, lowerProp);

            OWLClassExpression qualityDefinition = factory.getOWLObjectIntersectionOf(
                    concept, exactlyOneLower, exactlyOneUpper
            );

            OWLEquivalentClassesAxiom qualityEq = factory.getOWLEquivalentClassesAxiom(quality, qualityDefinition);
            manager.addAxiom(ontology, qualityEq);

            // Subclasses

            Set<OWLNamedIndividual> qualityIndividuals = new HashSet<>();

            for (QualityNames.Individuals.Quality qualityEnum : QualityNames.Individuals.Quality.values()) {
                OWLNamedIndividual qualityInd = factory.getOWLNamedIndividual(qualityEnum.iri().owlIri());
                qualityIndividuals.add(qualityInd);

                manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(quality, qualityInd));

                manager.addAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(lowerProp, qualityInd, qualityEnum.getLower()));
                manager.addAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(upperProp, qualityInd, qualityEnum.getUpper()));
            }

            OWLClassExpression qualityEnumeration = factory.getOWLObjectOneOf(qualityIndividuals);
            manager.addAxiom(ontology, factory.getOWLEquivalentClassesAxiom(quality,qualityEnumeration));
        }
    }
}
