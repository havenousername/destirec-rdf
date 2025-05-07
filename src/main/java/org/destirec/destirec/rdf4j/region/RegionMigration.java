package org.destirec.destirec.rdf4j.region;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
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
    private final DestiRecOntology destiRecOntology;
    private final IRI parentRegion = IRI.create(RegionNames.Classes.PARENT_REGION.pseudoUri());
    private final IRI leafRegion = IRI.create(RegionNames.Classes.LEAF_REGION.pseudoUri());
    private final IRI rootRegion = IRI.create(RegionNames.Classes.ROOT_REGION.pseudoUri());

    protected RegionMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology destiRecOntology) {
        super(rdf4jMethods, RegionNames.Classes.REGION.str());
        this.destiRecOntology = destiRecOntology;
    }

    class RegionPropertiesOntology {
        private final OWLDataFactory factory = destiRecOntology.getFactory();
        private final OWLOntologyManager manager = destiRecOntology.getManager();
        private final OWLClass region = destiRecOntology.getFactory().getOWLClass(get().stringValue());
        private final OWLOntology ontology = destiRecOntology.getOntology();
        private final OWLObjectProperty sfWithin = factory.getOWLObjectProperty(GEO.NAMESPACE + "sfWithin");
        private final OWLObjectProperty sfDirectlyWithin = factory.getOWLObjectProperty(GEO.NAMESPACE + "sfDirectlyWithin");
        private final OWLObjectProperty sfDirectlyContains = factory.getOWLObjectProperty(GEO.NAMESPACE + "sfDirectlyContains");


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

        // sfWithin ⊑ ¬sfWithin⁻¹
        public void defineDisjointAsymmetry() {
            OWLAxiom disjointInverse = factory.getOWLDisjointObjectPropertiesAxiom(sfWithin, factory.getOWLObjectInverseOf(sfWithin));
            manager.addAxiom(ontology, disjointInverse);
        }
    }

    class RegionOntology {
        OWLClass region = destiRecOntology.getFactory().getOWLClass(get().stringValue());
        OWLClass object = destiRecOntology.getFactory().getOWLClass(TopOntologyNames.Classes.OBJECT.owlIri());

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
            OWLObjectProperty sfDirectlyWithin = destiRecOntology.getFactory()
                    .getOWLObjectProperty(GEO.NAMESPACE + "sfDirectlyWithin");

            OWLObjectPropertyExpression sfContains = destiRecOntology.getFactory().getOWLObjectInverseOf(sfWithin);
            //  \exists sfWithin^{-1}.Region
            OWLClassExpression someSubregionsInRegion = destiRecOntology
                    .getFactory()
                    .getOWLObjectSomeValuesFrom(sfContains, region);
            //  \neg \exists sfWithin^{-1}.Region
            OWLClassExpression noSubRegions = destiRecOntology
                    .getFactory()
                    .getOWLObjectComplementOf(someSubregionsInRegion);

            // (>=1 \ sfWithin.Region)
            OWLClassExpression insideMoreOrOneRegion = destiRecOntology
                    .getFactory()
                    .getOWLObjectMinCardinality(1, sfWithin, region);

            //  (= 1 \ sfWDirectlyWithin.ParentRegion)
            OWLClassExpression insideOneRegionDirectly = destiRecOntology
                    .getFactory()
                    .getOWLObjectExactCardinality(1, sfDirectlyWithin, region);

            OWLClassExpression leafDefinition = destiRecOntology.getFactory()
                    .getOWLObjectIntersectionOf(region, insideMoreOrOneRegion,
                            insideOneRegionDirectly, noSubRegions);

            OWLClass attributesCollection = destiRecOntology.getFactory()
                    .getOWLClass(AttributeNames.Classes.ATTRIBUTES_COLLECTION.owlIri());

            OWLClassExpression leafRegionFull = destiRecOntology.getFactory()
                            .getOWLObjectIntersectionOf(attributesCollection, leafDefinition);

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

        RegionPropertiesOntology propertiesOntology = new RegionPropertiesOntology();
        propertiesOntology.defineSfWithinMapping();
        propertiesOntology.defineSfWithinIrreflexive();
        propertiesOntology.defineSfWithinOpposite();
        propertiesOntology.defineSfWithinTransitive();
        propertiesOntology.defineSfDirectlyWithin();
        propertiesOntology.defineDisjointAsymmetry();
    }

    @Override
    protected void setupProperties() {
        // Region \sqsubseteq Object
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, TopOntologyNames.Classes.OBJECT.rdfIri());

    }
}
