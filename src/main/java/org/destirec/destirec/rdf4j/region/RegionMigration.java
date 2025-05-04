package org.destirec.destirec.rdf4j.region;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.IriMigrationInstance;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Component;

@Component
@Getter
public class RegionMigration extends IriMigration implements OntologyDefiner {
    private IriMigrationInstance hasCost;
    private IriMigrationInstance hasMonths;
    private IriMigrationInstance hasFeatures;
    private final DestiRecOntology destiRecOntology;
    private final IRI parentRegion = IRI.create(RegionNames.Classes.PARENT_REGION.pseudoUri());
    private final IRI leafRegion = IRI.create(RegionNames.Classes.LEAF_REGION.pseudoUri());
    private final IRI rootRegion = IRI.create(RegionNames.Classes.ROOT_REGION.pseudoUri());

    protected RegionMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology destiRecOntology) {
        super(rdf4jMethods, RegionNames.Classes.REGION.str());
        this.destiRecOntology = destiRecOntology;
        initHasCost();
        initHasFeatures();
        initHasMonths();
        defineOntology();
    }

    class RegionPropertiesOntology {
        OWLDataFactory factory = destiRecOntology.getFactory();
        OWLOntologyManager manager = destiRecOntology.getManager();
        OWLClass region = destiRecOntology.getFactory().getOWLClass(get().stringValue());
        OWLOntology ontology = destiRecOntology.getOntology();
        OWLObjectProperty sfWithin = factory.getOWLObjectProperty(GEO.NAMESPACE + "sfWithin");
        OWLObjectProperty sfDirectlyWithin = factory.getOWLObjectProperty(GEO.NAMESPACE + "sfDirectlyWithin");
        OWLObjectProperty sfDirectlyContains = factory.getOWLObjectProperty(GEO.NAMESPACE + "sfDirectlyContains");


        // sfWithin ⊑ Region×Region
        public void defineSfWithinMapping() {
            manager.addAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(sfWithin, region));
            manager.addAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(sfWithin, region));
        }

        // sfWithin^−1≡sfContains
        public void defineSfWithinOpposite() {
            OWLObjectProperty sfContains = factory.getOWLObjectProperty(GEO.NAMESPACE + "sfContains");
            OWLObjectInverseOf inverseSfWithin = factory.getOWLObjectInverseOf(sfWithin);
            manager.addAxiom(ontology, factory.getOWLEquivalentObjectPropertiesAxiom(inverseSfWithin, sfContains));
        }

        // sfDirectlyWithin
        public void defineSfDirectlyWithin() {
            manager.addAxiom(ontology, factory.getOWLSubObjectPropertyOfAxiom(sfDirectlyWithin, sfWithin));
            manager.addAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(sfDirectlyWithin, region));
            manager.addAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(sfDirectlyWithin, region));
            // ≤1sfDirectlyWithin
            manager.addAxiom(ontology, factory.getOWLFunctionalObjectPropertyAxiom(sfDirectlyWithin));

            OWLObjectInverseOf inverseSfDirectlyWithin = factory.getOWLObjectInverseOf(sfWithin);
            manager.addAxiom(ontology, factory.getOWLEquivalentObjectPropertiesAxiom(inverseSfDirectlyWithin, sfDirectlyContains));
        }


        // sfWithin⊑+sfWithin
        public void defineSfWithinTransitive() {
            manager.addAxiom(ontology, factory.getOWLTransitiveObjectPropertyAxiom(sfWithin));
        }

        // ∀x.¬sfWithin(x,x) - irreflexive, region cannot contain itself - Germany cannot contain Germany
        public void defineSfWithinIrreflexive(){
            manager.addAxiom(ontology, factory.getOWLIrreflexiveObjectPropertyAxiom(sfWithin));
        }

    }

    class RegionOntology {
        OWLClass region = destiRecOntology.getFactory().getOWLClass(get().stringValue());
        OWLClass object = destiRecOntology.getFactory().getOWLClass(TopOntologyNames.Classes.OBJECT);

        OWLClass parentRegion = destiRecOntology.getFactory().getOWLClass(getParentRegion());
        OWLClass leafRegion = destiRecOntology.getFactory().getOWLClass(getLeafRegion());

        OWLClass feature = destiRecOntology.getFactory().getOWLClass(AttributeNames.Classes.FEATURE.owlIri());

        public void defineRegion() {
            // Region \sqsubseteq Object, region is subclass of object
            destiRecOntology.getManager().addAxiom(
                    destiRecOntology.getOntology(),
                    destiRecOntology.getFactory().getOWLSubClassOfAxiom(region, object)
            );
        }

        public void defineRegionParentOrLeaf() {
            // Region is either a parent region or a leaf region
            OWLClassExpression unionLeafParent = destiRecOntology.getFactory().getOWLObjectUnionOf(parentRegion, leafRegion);
            destiRecOntology.getManager().addAxiom(
                    destiRecOntology.getOntology(),
                    destiRecOntology.getFactory().getOWLEquivalentClassesAxiom(region, unionLeafParent)
            );
        }

        public void defineLeafRegion() {
            OWLObjectProperty sfWithin = destiRecOntology.getFactory().getOWLObjectProperty(GEO.NAMESPACE + "sfWithin");
            OWLObjectPropertyExpression sfContains = destiRecOntology.getFactory().getOWLObjectInverseOf(sfWithin);
            //  \exists sfWithin^{-1}.Region
            OWLClassExpression someSubregionsInRegion = destiRecOntology
                    .getFactory()
                    .getOWLObjectSomeValuesFrom(sfContains, region);
            //  \neg \exists sfWithin^{-1}.Region
            OWLClassExpression noSubRegions = destiRecOntology
                    .getFactory()
                    .getOWLObjectComplementOf(someSubregionsInRegion);

            // (=1 \ sfWithin.Region)
            OWLClassExpression insideOneRegion = destiRecOntology
                    .getFactory()
                    .getOWLObjectExactCardinality(1, sfWithin, region);

            OWLClassExpression leafDefinition = destiRecOntology.getFactory()
                    .getOWLObjectIntersectionOf(region, insideOneRegion, noSubRegions);

            // define for now that only leaf region has cost, months, and features
            OWLObjectProperty hasCost = destiRecOntology.getFactory().getOWLObjectProperty(AttributeNames.Properties.HAS_COST.owlIri());
            OWLClassExpression exactOneCost = destiRecOntology.getFactory()
                            .getOWLObjectExactCardinality(1, hasCost);

            OWLObjectProperty hasMonth = destiRecOntology.getFactory().getOWLObjectProperty(AttributeNames.Properties.HAS_MONTH.owlIri());
            OWLClassExpression exact12Months =destiRecOntology.getFactory()
                    .getOWLObjectExactCardinality(12, hasMonth);


            OWLObjectProperty hasFeature = destiRecOntology.getFactory().getOWLObjectProperty(AttributeNames.Properties.HAS_FEATURE.owlIri());
            OWLClassExpression existsFeature =  destiRecOntology.getFactory()
                    .getOWLObjectSomeValuesFrom(hasFeature, feature);

            OWLClassExpression leafProperties = destiRecOntology.getFactory()
                            .getOWLObjectIntersectionOf(exact12Months, exactOneCost, existsFeature);


            OWLClassExpression leafRegionFull = destiRecOntology.getFactory()
                            .getOWLObjectIntersectionOf(leafProperties, leafDefinition);

            // Define LeafRegion
            destiRecOntology.getManager()
                    .addAxiom(
                            destiRecOntology.getOntology(),
                            destiRecOntology.getFactory()
                                    .getOWLEquivalentClassesAxiom(leafRegion, leafRegionFull)
                    );
        }

        public void defineParentRegion() {
            OWLObjectProperty sfWithin = destiRecOntology.getFactory().getOWLObjectProperty(GEO.NAMESPACE + "sfWithin");
            OWLObjectPropertyExpression sfContains = destiRecOntology.getFactory().getOWLObjectInverseOf(sfWithin);
            // \forall sfWithin^{-1}.Region, all sfWithin contain regions
            OWLClassExpression containsOnlyRegions = destiRecOntology.getFactory().getOWLObjectAllValuesFrom(sfContains, region);
            OWLClassExpression containsMoreThanZero = destiRecOntology
                    .getFactory()
                    .getOWLObjectMinCardinality(1, sfContains, region);
            OWLClassExpression insideOneOrLess = destiRecOntology
                    .getFactory()
                    .getOWLObjectMaxCardinality(1, sfWithin, region);

            OWLClassExpression parentDefinition = destiRecOntology
                    .getFactory()
                    .getOWLObjectIntersectionOf(region, containsOnlyRegions, containsMoreThanZero, insideOneOrLess);

            // Define equivalence
            destiRecOntology.getManager()
                    .addAxiom(
                            destiRecOntology.getOntology(),
                            destiRecOntology.getFactory()
                                    .getOWLEquivalentClassesAxiom(parentRegion, parentDefinition)
                    );
        }
    }

    @Override
    public void defineOntology() {
        RegionOntology ontology = new RegionOntology();
        ontology.defineRegion();
        ontology.defineRegionParentOrLeaf();
        ontology.defineLeafRegion();
        ontology.defineParentRegion();
    }

    private void initHasCost() {
        hasCost = new IriMigrationInstance(
                rdf4jMethods, RegionNames.Properties.HAS_COST.str(),
                (instance) -> instance
                        .builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.OBJECTPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.RANGE, AttributeNames.Classes.COST.rdfIri())
                        .add(instance.predicate(), RDFS.LABEL, "connect to cost")
        );
    }


    private void initHasMonths() {
        hasMonths = new IriMigrationInstance(
                rdf4jMethods, RegionNames.Properties.HAS_MONTH.str(),
                (instance) -> instance
                        .builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.OBJECTPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.RANGE, AttributeNames.Classes.MONTH.rdfIri())
                        .add(instance.predicate(), RDFS.LABEL, "connect to region months")
        );
    }

    private void initHasFeatures() {
        hasFeatures = new IriMigrationInstance(
                rdf4jMethods, RegionNames.Properties.HAS_FEATURE.str(),
                (instance) -> instance
                        .builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.OBJECTPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.RANGE, AttributeNames.Classes.FEATURE.rdfIri())
                        .add(instance.predicate(), RDFS.LABEL, "connect to region features")
        );
    }

    @Override
    protected void setupProperties() {
        // Region \sqsubseteq Object
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, TopOntologyNames.Classes.OBJECT);

    }

    @Override
    public void setup() {
        super.setup();
        hasCost.setup();
        hasMonths.setup();
        hasFeatures.setup();
    }

    @Override
    public void migrate() {
        super.migrate();
        hasCost.migrate();
        hasMonths.migrate();
        hasFeatures.migrate();
    }
}
