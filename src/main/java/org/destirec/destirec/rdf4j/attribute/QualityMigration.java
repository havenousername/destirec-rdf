package org.destirec.destirec.rdf4j.attribute;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.region.RegionService;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.QualityNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Component
public class QualityMigration extends IriMigration implements OntologyDefiner {
    private final DestiRecOntology destirecOntology;
    private final RegionService regionService;
    public QualityMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology ontology, RegionService regionService) {
        super(rdf4jMethods, QualityNames.Classes.QUALITY.str());
        this.destirecOntology = ontology;
        this.regionService = regionService;
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, TopOntologyNames.Classes.CONCEPT.rdfIri());
    }

    @Override
    public void defineOntology() {
        QualityOntology ontology = new QualityOntology();
        ontology.defineQuality();
        ontology.defineHasQuality();
        ontology.defineRegionQualities();
    }

    private class QualityOntology {
        private final OWLDataFactory factory = destirecOntology.getFactory();
        private final OWLOntologyManager manager = destirecOntology.getManager();

        private final OWLOntology ontology = destirecOntology.getOntology();

        private final OWLClass quality = factory.getOWLClass(QualityNames.Classes.QUALITY.owlIri());

        private final OWLClass feature = factory.getOWLClass(AttributeNames.Classes.FEATURE.owlIri());
        private final OWLClass concept = factory.getOWLClass(TopOntologyNames.Classes.CONCEPT.owlIri());

        private final OWLObjectProperty hasFeature = factory.getOWLObjectProperty(AttributeNames.Properties.HAS_FEATURE.owlIri());

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

        public void defineRegionQualities() {
            List<RegionDto> regions = regionService.getLeafRegions();
            OWLObjectProperty sfDirectlyWithin = factory.getOWLObjectProperty(GEO.NAMESPACE + "sfDirectlyWithin");
            for (var region : regions) {
                OWLNamedIndividual regionInd = factory.getOWLNamedIndividual(region.id.stringValue());
                for (var feature : region.getFeatures()) {
                    int score = feature.getHasScore();
                    for (QualityNames.Individuals.Quality qualityEnum : QualityNames.Individuals.Quality.values()) {
                        OWLNamedIndividual qualityInd = factory.getOWLNamedIndividual(qualityEnum.iri().owlIri());

                        int lower = qualityEnum.getLower();
                        int upper = qualityEnum.getUpper();

                        if (score > lower && score <= upper) {
                            String featureQuality = "has"
                                    + StringUtils.capitalize(feature.getRegionFeature().name())
                                    + "Quality";
                            OWLObjectProperty hasFQuality = factory.getOWLObjectProperty(featureQuality);

                            manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(hasFQuality, regionInd, qualityInd));


                            // hasFQuality \sqsubseteq hasQuality
                            manager.addAxiom(ontology, factory.getOWLSubObjectPropertyOfAxiom(hasFQuality, hasFeature));
                            // sfDirectlyWithin \ \circ  has{F}Quality  \sqsubseteq has{F}Quality - for the inference
                            // on the parent level
                            OWLSubPropertyChainOfAxiom propertyChainAxiom = factory
                                    .getOWLSubPropertyChainOfAxiom(List.of(sfDirectlyWithin, hasFQuality), hasFQuality);
                            manager.addAxiom(ontology, propertyChainAxiom);
                        }
                    }
                }
            }
        }

        public void defineHasQuality() {
            // hasQuality \equiv Feature \times Quality
            manager.addAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(hasFeature, feature));
            manager.addAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(hasFeature, quality));
        }

    }
}
