package org.destirec.destirec.rdf4j.attribute;

import org.apache.commons.lang.StringUtils;
import org.destirec.destirec.rdf4j.ontology.AppOntology;
import org.destirec.destirec.rdf4j.ontology.OntologyFeature;
import org.destirec.destirec.rdf4j.poi.POIDto;
import org.destirec.destirec.rdf4j.preferences.PreferenceDto;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.QualityNames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QualityOntology {
    private final OWLDataFactory factory;
    private final AppOntology ontology;

    private final OWLClass quality;
    private final OWLClass feature;
    private final OWLClass concept;
    private final OWLObjectProperty hasQuality;
    private final RDF4JTemplate template;

    private final SimpleValueFactory valueFactory;


    public QualityOntology(AppOntology ontology, OWLDataFactory factory, RDF4JTemplate template) {
        this.factory = factory;
        this.ontology = ontology;

        this.quality = factory.getOWLClass(QualityNames.Classes.QUALITY.owlIri());
        this.feature = factory.getOWLClass(AttributeNames.Classes.FEATURE.owlIri());
        this.concept = factory.getOWLClass(TopOntologyNames.Classes.CONCEPT.owlIri());

        this.hasQuality = factory.getOWLObjectProperty(AttributeNames.Properties.HAS_QUALITY.owlIri());
        this.template = template;
        valueFactory = SimpleValueFactory.getInstance();
    }

    public void defineQuality() {
        OWLDataPropertyExpression upperProp = factory.getOWLDataProperty(QualityNames.Properties.UPPER.owlIri());
        OWLDataPropertyExpression lowerProp = factory.getOWLDataProperty(QualityNames.Properties.LOWER.owlIri());

        // Quality ≡ Concept ⊓ (=1 lower) ⊓ (=1 upper)
        OWLClassExpression exactlyOneUpper = factory.getOWLDataSomeValuesFrom(upperProp, OWL2Datatype.XSD_UNSIGNED_INT);
        OWLClassExpression exactlyOneLower = factory.getOWLDataSomeValuesFrom(lowerProp, OWL2Datatype.XSD_UNSIGNED_INT);

        OWLClassExpression qualityDefinition = factory.getOWLObjectIntersectionOf(
                concept, exactlyOneLower, exactlyOneUpper
        );

        OWLEquivalentClassesAxiom qualityEq = factory.getOWLEquivalentClassesAxiom(quality, qualityDefinition);
        ontology.addAxiom( qualityEq);

        // Subclasses

        Set<OWLNamedIndividual> qualityIndividuals = new HashSet<>();

        for (QualityNames.Individuals.Quality qualityEnum : QualityNames.Individuals.Quality.values()) {
            OWLNamedIndividual qualityInd = factory.getOWLNamedIndividual(qualityEnum.iri().owlIri());
            qualityIndividuals.add(qualityInd);

            ontology.addAxiom(factory.getOWLClassAssertionAxiom(quality, qualityInd));

            ontology.addAxiom(factory.getOWLDataPropertyAssertionAxiom(lowerProp, qualityInd, qualityEnum.getLower()));
            ontology.addAxiom(factory.getOWLDataPropertyAssertionAxiom(upperProp, qualityInd, qualityEnum.getUpper()));
        }

        OWLClassExpression qualityEnumeration = factory.getOWLObjectOneOf(qualityIndividuals);
        ontology.addAxiom(factory.getOWLEquivalentClassesAxiom(quality,qualityEnumeration));
    }

    public void defineRegionsQualities(List<RegionDto> regions) {
        defineRegionsQualities(regions, OntologyFeature.GENERAL.toString());
    }

    public void definePreferenceQualities(PreferenceDto preferenceDto, String ontologyFeature) {
        var features = preferenceDto.getFeatureDtos();
        OWLNamedIndividual regionInd = factory.getOWLNamedIndividual(preferenceDto.id().stringValue());
        OWLObjectProperty forFeature = factory.getOWLObjectProperty(RegionNames.Properties.FOR_FEATURE.owlIri());
        for (var feature : features) {
            int score = feature.getHasScore();
            OWLIndividual featureInd = factory.getOWLNamedIndividual(feature.getId().stringValue());
            for (QualityNames.Individuals.Quality qualityEnum : QualityNames.Individuals.Quality.values()) {
                OWLNamedIndividual qualityInd = factory.getOWLNamedIndividual(qualityEnum.iri().owlIri());

                int lower = qualityEnum.getLower();
                int upper = qualityEnum.getUpper();

                if (score >= lower && score < upper) {
                    String featureQuality = "has"
                            + StringUtils.capitalize(feature.getRegionFeature().name())
                            + "Quality";

                    OWLObjectProperty hasFQuality = factory.getOWLObjectProperty(DESTIREC.wrap(featureQuality).owlIri());

                    ontology.addAxiom(factory.getOWLObjectPropertyAssertionAxiom(hasFQuality, regionInd, qualityInd), ontologyFeature);

                    // hasFQuality \sqsubseteq hasQuality
                    ontology.addAxiom(factory.getOWLSubObjectPropertyOfAxiom(hasFQuality, hasQuality), ontologyFeature);

                    OWLIndividual subject = factory.getOWLNamedIndividual(DESTIREC.wrap(featureQuality).pseudoUri());
                    // hasFQuality :forFeature :Feature
                    ontology.addAxiom(factory.getOWLObjectPropertyAssertionAxiom(forFeature, subject, featureInd), ontologyFeature);
                }
            }
        }
    }

    public void definePOIOntology(List<POIDto> pois, String ontologyFeature) {
        OWLObjectProperty sfDirectlyContains = factory.getOWLObjectProperty(RegionNames.Properties.SF_D_CONTAINS);
        OWLObjectProperty forFeature = factory.getOWLObjectProperty(RegionNames.Properties.FOR_FEATURE.owlIri());
        for (var poiDto : pois) {
            OWLNamedIndividual poiInd = factory.getOWLNamedIndividual(poiDto.getId().stringValue());
            if (poiDto.getFeature() == null) {
                continue;
            }
            FeatureDto featureDto = poiDto.getFeature();
            int score = featureDto.getHasScore();
            OWLIndividual featureInd = factory.getOWLNamedIndividual(featureDto.id().stringValue());
            for (QualityNames.Individuals.Quality qualityEnum : QualityNames.Individuals.Quality.values()) {
                OWLNamedIndividual qualityInd = factory.getOWLNamedIndividual(qualityEnum.iri().owlIri());

                int lower = qualityEnum.getLower();
                int upper = qualityEnum.getUpper();

                if (score >= lower && score < upper) {
                    String featureQuality = "has"
                            + StringUtils.capitalize(featureDto.getRegionFeature().name())
                            + "Quality";

                    OWLObjectProperty hasFQuality = factory.getOWLObjectProperty(DESTIREC.wrap(featureQuality).owlIri());
                    // sfDirectlyWithin \ \circ  has{F}Quality  \sqsubseteq has{F}Quality - for the inference
                    // on the parent level
                    OWLSubPropertyChainOfAxiom propertyChainAxiom = factory
                            .getOWLSubPropertyChainOfAxiom(List.of(sfDirectlyContains, hasFQuality), hasFQuality);

                    ontology.addAxiom(factory.getOWLObjectPropertyAssertionAxiom(hasFQuality, poiInd, qualityInd), ontologyFeature);

                    // hasFQuality \sqsubseteq hasQuality
                    ontology.addAxiom(factory.getOWLSubObjectPropertyOfAxiom(hasFQuality, hasQuality), ontologyFeature);
                    ontology.addAxiom(propertyChainAxiom, ontologyFeature);

                    OWLIndividual subject = factory.getOWLNamedIndividual(DESTIREC.wrap(featureQuality).pseudoUri());
                    // hasFQuality :forFeature :Feature
                    ontology.addAxiom(factory.getOWLObjectPropertyAssertionAxiom(forFeature, subject, featureInd), ontologyFeature);
                }
            }
        }
    }

    public void defineRegionsQualities(List<RegionDto> regions, String ontologyFeature) {
        OWLObjectProperty sfDirectlyContains = factory.getOWLObjectProperty(RegionNames.Properties.SF_D_CONTAINS);
        OWLObjectProperty forFeature = factory.getOWLObjectProperty(RegionNames.Properties.FOR_FEATURE.owlIri());
        for (var region : regions) {
            boolean isParent = template.applyToConnection(connection ->
                connection.hasStatement(region.getId(), valueFactory.createIRI(RegionNames.Properties.SF_D_CONTAINS), null, true)
            );
            OWLNamedIndividual regionInd = factory.getOWLNamedIndividual(region.id.stringValue());


            if (!isParent) {
                var features = region.getFeatures();
                for (var feature : features) {
                    int score = feature.getHasScore();
                    OWLIndividual featureInd = factory.getOWLNamedIndividual(feature.getId().stringValue());
                    for (QualityNames.Individuals.Quality qualityEnum : QualityNames.Individuals.Quality.values()) {
                        OWLNamedIndividual qualityInd = factory.getOWLNamedIndividual(qualityEnum.iri().owlIri());

                        int lower = qualityEnum.getLower();
                        int upper = qualityEnum.getUpper();

                        if (score >= lower && score < upper) {
                            String featureQuality = "has"
                                    + StringUtils.capitalize(feature.getRegionFeature().name())
                                    + "Quality";

                            OWLObjectProperty hasFQuality = factory.getOWLObjectProperty(DESTIREC.wrap(featureQuality).owlIri());
                            // sfDirectlyWithin \ \circ  has{F}Quality  \sqsubseteq has{F}Quality - for the inference
                            // on the parent level
                            OWLSubPropertyChainOfAxiom propertyChainAxiom = factory
                                    .getOWLSubPropertyChainOfAxiom(List.of(sfDirectlyContains, hasFQuality), hasFQuality);

                            ontology.addAxiom(factory.getOWLObjectPropertyAssertionAxiom(hasFQuality, regionInd, qualityInd), ontologyFeature);

                            // hasFQuality \sqsubseteq hasQuality
                            ontology.addAxiom(factory.getOWLSubObjectPropertyOfAxiom(hasFQuality, hasQuality), ontologyFeature);
                            ontology.addAxiom(propertyChainAxiom, ontologyFeature);

                            OWLIndividual subject = factory.getOWLNamedIndividual(DESTIREC.wrap(featureQuality).pseudoUri());
                            // hasFQuality :forFeature :Feature
                            ontology.addAxiom(factory.getOWLObjectPropertyAssertionAxiom(forFeature, subject, featureInd), ontologyFeature);
                        }
                    }
                }
            } else {
                ontology.removeDatabaseAxioms(region.id().stringValue(), region.id());
            }
        }
    }

    public void defineHasQuality() {
        // hasQuality \equiv Feature \times Quality
        ontology.addAxiom(factory.getOWLObjectPropertyDomainAxiom(hasQuality, feature));
        ontology.addAxiom(factory.getOWLObjectPropertyRangeAxiom(hasQuality, quality));
    }

}