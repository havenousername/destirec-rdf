package org.destirec.destirec.rdf4j.region.feature;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.IriMigrationInstance;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.vocabulary.DBPEDIA;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Getter
public class FeatureMigration extends IriMigration implements OntologyDefiner {
    private final DestiRecOntology destiRecOntology;
    private IriMigrationInstance hasRegionFeature;

    public FeatureMigration(RDF4JTemplate rdf4JTemplate, DestiRecOntology destiRecOntology) {
        super(rdf4JTemplate, AttributeNames.Classes.FEATURE.str());
        this.destiRecOntology = destiRecOntology;
        initHasRegionFeaturePredicate();
    }

    private void initHasRegionFeaturePredicate() {
        hasRegionFeature = new IriMigrationInstance(
                rdf4jMethods, AttributeNames.Properties.HAS_REGION_FEATURE.str(),
                instance -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.OBJECTPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.RANGE, RegionFeatureNames.Classes.REGION_FEATURE.pseudoUri())
        );
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(get(), RDFS.SUBCLASSOF, TopOntologyNames.Classes.CONCEPT.rdfIri())
                .add(get(), SKOS.RELATED_MATCH, DBPEDIA.INTEREST);
    }

    @Override
    public void setup() {
        super.setup();
        hasRegionFeature.setup();
    }

    @Override
    public void migrate() {
        super.migrate();
        hasRegionFeature.migrate();
    }


    @Override
    public void defineOntology() {
        var featuresOntology = new FeatureOntology();
        featuresOntology.defineFeature();
        featuresOntology.defineRegionFeatures();
        featuresOntology.defineHasRegionFeature();
    }

    class FeatureOntology {
        OWLClass attribute = destiRecOntology
                .getFactory()
                .getOWLClass(AttributeNames.Classes.ATTRIBUTE.pseudoUri());

        OWLClass feature = destiRecOntology
                .getFactory()
                .getOWLClass(AttributeNames.Classes.FEATURE.pseudoUri());

        OWLClass regionFeature = destiRecOntology
                .getFactory()
                .getOWLClass(RegionFeatureNames.Classes.REGION_FEATURE.pseudoUri());

        OWLObjectPropertyExpression hasRegionFeature = destiRecOntology
                .getFactory()
                .getOWLObjectProperty(AttributeNames.Properties.HAS_REGION_FEATURE.pseudoUri());


        public void defineFeature() {
            // exist one hasRegionFeature

            OWLClassExpression hasExactlyOneRegionFeature = destiRecOntology.getFactory().getOWLObjectExactCardinality(1, hasRegionFeature);
            OWLObjectAllValuesFrom allValuesAreRegionFeature = destiRecOntology.getFactory().getOWLObjectAllValuesFrom(hasRegionFeature, regionFeature);

            OWLClassExpression intersectionScoredAttribute = destiRecOntology.getFactory().getOWLObjectIntersectionOf(
                attribute,
                hasExactlyOneRegionFeature,
                allValuesAreRegionFeature
            );

            destiRecOntology
                    .addAxiom(
                            destiRecOntology.getFactory().getOWLEquivalentClassesAxiom(
                                    feature,
                                    intersectionScoredAttribute
                            )
                    );
        }

        public void defineHasRegionFeature() {
            destiRecOntology.addAxiom(
                    destiRecOntology.getFactory().getOWLFunctionalObjectPropertyAxiom(hasRegionFeature)
            );


            var regionNames = Arrays.stream(RegionFeatureNames.Individuals.RegionFeature.values()).map(i ->
                    destiRecOntology.getFactory().getOWLNamedIndividual(i.iri().owlIri()))
                    .collect(Collectors.toSet());
            OWLClassExpression regionFeatureNominals = destiRecOntology.getFactory().getOWLObjectOneOf(regionNames);

            destiRecOntology.addAxiom(
                    destiRecOntology.getFactory().getOWLObjectPropertyRangeAxiom(hasRegionFeature, regionFeatureNominals)
            );
        }

        private OWLClassExpression getRegionFeatures() {

            Set<OWLIndividual> featureIndividuals = Arrays.stream(RegionFeatureNames.Individuals.RegionFeature.values())
                    .map(ind -> destiRecOntology.getFactory().getOWLNamedIndividual(ind.iri().pseudoUri()))
                    .collect(Collectors.toSet());
            return destiRecOntology.getFactory().getOWLObjectOneOf(featureIndividuals);
        }

        public void defineRegionFeatures() {
            OWLClassExpression regionFeatures = getRegionFeatures();
            OWLEquivalentClassesAxiom axiom = destiRecOntology.getFactory().getOWLEquivalentClassesAxiom(regionFeature, regionFeatures);
            destiRecOntology.addAxiom(axiom);
        }
    }
}
