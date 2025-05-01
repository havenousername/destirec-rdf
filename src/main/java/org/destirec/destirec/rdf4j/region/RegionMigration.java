package org.destirec.destirec.rdf4j.region;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.IriMigrationInstance;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
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
    private final IRI parentRegion = IRI.create(RegionNames.Classes.PARENT_REGION);
    private final IRI leafRegion = IRI.create(RegionNames.Classes.LEAF_REGION);
    private final IRI rootRegion = IRI.create(RegionNames.Classes.ROOT_REGION);

    protected RegionMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology destiRecOntology) {
        super(rdf4jMethods, "Region");
        this.destiRecOntology = destiRecOntology;
        initHasCost();
        initHasFeatures();
        initHasMonths();
        defineOntology();
    }

    class RegionOntology {
        OWLClass region = destiRecOntology.getFactory().getOWLClass(get().stringValue());
        OWLClass object = destiRecOntology.getFactory().getOWLClass(TopOntologyNames.Classes.OBJECT);

        OWLClass parentRegion = destiRecOntology.getFactory().getOWLClass(getParentRegion());
        OWLClass leafRegion = destiRecOntology.getFactory().getOWLClass(getLeafRegion());

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

            // Define LeafRegion
            destiRecOntology.getManager()
                    .addAxiom(
                            destiRecOntology.getOntology(),
                            destiRecOntology.getFactory()
                                    .getOWLEquivalentClassesAxiom(leafRegion, leafDefinition)
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

    private void defineOntology() {
        RegionOntology ontology = new RegionOntology();
        ontology.defineRegion();
        ontology.defineRegionParentOrLeaf();
        ontology.defineLeafRegion();
        ontology.defineParentRegion();
    }

    private void initHasCost() {
        hasCost = new IriMigrationInstance(
                rdf4jMethods, "hasCost",
                (instance) -> instance
                        .builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.LABEL, "connect to cost")
        );
    }


    private void initHasMonths() {
        hasMonths = new IriMigrationInstance(
                rdf4jMethods, "hasMonths",
                (instance) -> instance
                        .builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.LABEL, "connect to region months")
        );
    }

    private void initHasFeatures() {
        hasFeatures = new IriMigrationInstance(
                rdf4jMethods, "hasFeatures",
                (instance) -> instance
                        .builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
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
