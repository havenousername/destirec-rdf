package org.destirec.destirec.rdf4j.attribute;

import org.apache.commons.lang.StringUtils;
import org.destirec.destirec.rdf4j.region.RegionDao;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.QualityNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QualityOntology {
    private final OWLDataFactory factory;
    private final OWLOntologyManager manager;
    private final OWLOntology ontology;

    private final OWLClass quality;
    private final OWLClass feature;
    private final OWLClass concept;
    private final OWLObjectProperty hasQuality;

    private final RegionDao regionDao;

    public QualityOntology(OWLDataFactory factory, OWLOntologyManager manager, OWLOntology ontology, RegionDao regionDao) {
        this.factory = factory;
        this.manager = manager;
        this.ontology = ontology;

        this.quality = factory.getOWLClass(QualityNames.Classes.QUALITY.owlIri());
        this.feature = factory.getOWLClass(AttributeNames.Classes.FEATURE.owlIri());
        this.concept = factory.getOWLClass(TopOntologyNames.Classes.CONCEPT.owlIri());

        this.hasQuality = factory.getOWLObjectProperty(AttributeNames.Properties.HAS_QUALITY.owlIri());
        this.regionDao = regionDao;
    }

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

    public void defineRegionsQualities(List<RegionDto> regions) {
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

                        OWLObjectProperty hasFQuality = factory.getOWLObjectProperty(DESTIREC.wrap(featureQuality).owlIri());

                        manager.addAxiom(ontology, factory.getOWLObjectPropertyAssertionAxiom(hasFQuality, regionInd, qualityInd));

                        // hasFQuality \sqsubseteq hasQuality
                        manager.addAxiom(ontology, factory.getOWLSubObjectPropertyOfAxiom(hasFQuality, hasQuality));
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

    public void defineRegionsQualities() {
        List<RegionDto> regions = regionDao.list();
        defineRegionsQualities(regions);
    }

    public void defineHasQuality() {
        // hasQuality \equiv Feature \times Quality
        manager.addAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(hasQuality, feature));
        manager.addAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(hasQuality, quality));
    }

}