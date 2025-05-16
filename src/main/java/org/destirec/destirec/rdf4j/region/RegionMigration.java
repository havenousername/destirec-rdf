package org.destirec.destirec.rdf4j.region;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
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

    protected RegionMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology destiRecOntology) {
        super(rdf4jMethods, RegionNames.Classes.REGION.str());
        this.destiRecOntology = destiRecOntology;
    }

    class RegionPropertiesOntology {
        private final OWLDataFactory factory = destiRecOntology.getFactory();
        private final OWLOntologyManager manager = destiRecOntology.getManager();
        private final OWLClass region = destiRecOntology.getFactory().getOWLClass(RegionNames.Classes.REGION.owlIri());

//        private final OWLClass parentRegion = destiRecOntology.getFactory().getOWLClass(RegionNames.Classes.PARENT_REGION.owlIri());
        private final OWLOntology ontology = destiRecOntology.getOntology();
        private final OWLObjectProperty sfWithin = factory.getOWLObjectProperty(RegionNames.Properties.SF_WITHIN);
        private final OWLObjectProperty sfContains = factory.getOWLObjectProperty(RegionNames.Properties.SF_CONTAINS);
        private final OWLObjectProperty sfDirectlyWithin = factory.getOWLObjectProperty(RegionNames.Properties.SF_D_WITHIN);
        private final OWLObjectProperty sfDirectlyContains = factory.getOWLObjectProperty(RegionNames.Properties.SF_D_CONTAINS);


        // sfWithin ⊑ Region×Region
        public void defineSfWithinMapping() {
            manager.addAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(sfWithin, region));
            manager.addAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(sfWithin, region));
        }

        // sfWithin^−1≡sfContains
        public void defineSfWithinOpposite() {
            OWLAxiom inverseSfWithin = factory.getOWLInverseObjectPropertiesAxiom(sfWithin, sfContains);
            manager.addAxiom(ontology, inverseSfWithin);
        }

        // sfDirectlyWithin
        public void defineSfDirectlyWithin() {
            manager.addAxiom(ontology, factory.getOWLSubObjectPropertyOfAxiom(sfDirectlyWithin, sfWithin));
            manager.addAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(sfDirectlyWithin, region));
            manager.addAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(sfDirectlyWithin, region));
            // ≤1sfDirectlyWithin
            manager.addAxiom(ontology, factory.getOWLFunctionalObjectPropertyAxiom(sfDirectlyWithin));

            OWLAxiom inverseAxiom = factory.getOWLInverseObjectPropertiesAxiom(sfDirectlyContains, sfDirectlyWithin);
            manager.addAxiom(ontology, inverseAxiom);
        }


        // sfWithin⊑+sfWithin
        public void defineSfWithinTransitive() {
            manager.addAxiom(ontology, factory.getOWLTransitiveObjectPropertyAxiom(sfWithin));
        }

//        // ∀x.¬sfWithin(x,x) - irreflexive, region cannot contain itself - Germany cannot contain Germany
        public void defineSfWithinIrreflexive(){
            manager.addAxiom(ontology, factory.getOWLIrreflexiveObjectPropertyAxiom(sfDirectlyWithin));
        }

        // sfWithin ⊑ ¬sfWithin⁻¹
        public void defineDisjointAsymmetry() {
            OWLAxiom disjointInverse = factory.getOWLDisjointObjectPropertiesAxiom(sfDirectlyWithin, factory.getOWLObjectInverseOf(sfDirectlyWithin));
            manager.addAxiom(ontology, disjointInverse);
        }
    }

    class RegionOntology {
        OWLClass region = destiRecOntology.getFactory().getOWLClass(RegionNames.Classes.REGION.owlIri());
        OWLClass object = destiRecOntology.getFactory().getOWLClass(TopOntologyNames.Classes.OBJECT.owlIri());

        OWLClass parentRegion = destiRecOntology.getFactory().getOWLClass(RegionNames.Classes.PARENT_REGION.owlIri());
        OWLClass leafRegion = destiRecOntology.getFactory().getOWLClass(RegionNames.Classes.LEAF_REGION.owlIri());


        public void defineRegion() {
            // Region \sqsubseteq Object, region is subclass of object
            destiRecOntology.getManager().addAxiom(
                    destiRecOntology.getOntology(),
                    destiRecOntology.getFactory().getOWLSubClassOfAxiom(region, object)
            );
        }

        public void defineRegionParentOrLeaf() {
            // Region is union of a parent region or a leaf region
            OWLClassExpression unionLeafParent = destiRecOntology.getFactory().getOWLObjectUnionOf(parentRegion, leafRegion);
            destiRecOntology.getManager().addAxiom(
                    destiRecOntology.getOntology(),
                    destiRecOntology.getFactory().getOWLEquivalentClassesAxiom(region, unionLeafParent)
            );

            OWLDisjointClassesAxiom disjoint = destiRecOntology.getFactory().getOWLDisjointClassesAxiom(parentRegion, leafRegion);
            destiRecOntology.getManager().addAxiom(
                    destiRecOntology.getOntology(),
                    disjoint
            );

            destiRecOntology.getManager().addAxiom(
                    destiRecOntology.getOntology(),
                    destiRecOntology.getFactory().getOWLSubClassOfAxiom(parentRegion, region)
            );

            destiRecOntology.getManager().addAxiom(
                    destiRecOntology.getOntology(),
                    destiRecOntology.getFactory().getOWLSubClassOfAxiom(leafRegion, region)
            );
        }

        // LeafRegion \equiv AttributesCollection
        // \sqcap \neg \exists sfDirectlyWithin^{-1}.Region
        // \sqcap = 1 \ sfDirectlyWithin.ParentRegion
        public void defineLeafRegion() {
            OWLObjectProperty sfDirectlyWithin = destiRecOntology.getFactory()
                    .getOWLObjectProperty(RegionNames.Properties.SF_D_WITHIN);
            OWLObjectProperty sfDirectlyContains = destiRecOntology.getFactory()
                    .getOWLObjectProperty(RegionNames.Properties.SF_D_CONTAINS);

//            OWLObjectPropertyExpression sfWithinInverse = destiRecOntology.getFactory()
//                    .getOWLObjectInverseOf(sfDirectlyWithin);
//
//            OWLObjectSomeValuesFrom someInverseSfWithinRegion = destiRecOntology.getFactory()
//                    .getOWLObjectSomeValuesFrom(sfDirectlyContains, region);
//
//            OWLObjectComplementOf notSomeInverseSfWithinRegion = destiRecOntology.getFactory()
//                    .getOWLObjectComplementOf(someInverseSfWithinRegion);

//            OWLObjectPropertyExpression sfWithinInverse = destiRecOntology.getFactory()
//                    .getOWLObjectInverseOf(sfDirectlyWithin);
            OWLClassExpression noSubregions = destiRecOntology.getFactory()
                    .getOWLObjectMaxCardinality(0, sfDirectlyContains, region);

            OWLClassExpression exactlyOneParent = destiRecOntology.getFactory()
                    .getOWLObjectExactCardinality(1, sfDirectlyWithin, region);
            OWLClass attributesCollection = destiRecOntology.getFactory()
                    .getOWLClass(AttributeNames.Classes.ATTRIBUTES_COLLECTION.owlIri());

            OWLObjectIntersectionOf intersection =  destiRecOntology.getFactory().getOWLObjectIntersectionOf(
                    region,
                    attributesCollection,
//                    notSomeInverseSfWithinRegion,
//                    exactlyOneParent,
                    destiRecOntology.getFactory()
                            .getOWLObjectComplementOf(parentRegion)
//                    noSubregions
//                    insideOneRegionDirectly
//                    containsNone
            );


//            OWLClassExpression leafRegionFull = destiRecOntology.getFactory()
//                            .getOWLObjectIntersectionOf(attributesCollection, intersection);

            // Define LeafRegion
            destiRecOntology.getManager()
                    .addAxiom(
                            destiRecOntology.getOntology(),
                            destiRecOntology.getFactory()
                                    .getOWLEquivalentClassesAxiom(leafRegion, intersection)
                    );

//            OWLClassExpression closureRestriction = destiRecOntology.getFactory()
//                    .getOWLObjectMaxCardinality(0, sfDirectlyContains, region);
//            OWLAxiom closureAxiom = destiRecOntology.getFactory()
//                    .getOWLSubClassOfAxiom(leafRegion, closureRestriction);
//            destiRecOntology.getManager().addAxiom(destiRecOntology.getOntology(), closureAxiom);
        }

        // ParentRegion ≡ InternalRegion ⊔ RootRegion
        // RootRegion ≡ ∀ sfWithin⁻¹.Region ⊓ ∃ sfWithin⁻¹.Region ⊓ ¬∃ sfWithin.Region
        // InternalRegion ≡ ∀ sfWithin⁻¹.Region ⊓ ∃ sfWithin⁻¹.Region ⊓ ∃ sfWithin.Region
        // RootRegion ⊓ InternalRegion ⊑ ⊥
        public void defineParentRegion() {
            OWLDataFactory factory = destiRecOntology.getFactory();

            OWLObjectProperty sfDirectlyWithin = factory.getOWLObjectProperty(RegionNames.Properties.SF_D_WITHIN);
            OWLObjectProperty sfDirectlyContains = factory.getOWLObjectProperty(RegionNames.Properties.SF_D_CONTAINS);
            OWLObjectPropertyExpression sfWithinInverse = factory.getOWLObjectInverseOf(sfDirectlyWithin);

            OWLClass attributesCollection = factory.getOWLClass(AttributeNames.Classes.ATTRIBUTES_COLLECTION.owlIri());

            // ∀ sfWithin⁻¹.Region
            OWLClassExpression onlyIncomingFromRegions = factory.getOWLObjectAllValuesFrom(sfWithinInverse, region);

            // ∃ sfWithin⁻¹.Region
            OWLClassExpression someIncomingFromRegions = factory.getOWLObjectSomeValuesFrom(sfWithinInverse, region);

            // ∃ sfDirectlyContains⁻¹.Region
            OWLClassExpression someDirectlyContains = factory.getOWLObjectSomeValuesFrom(sfDirectlyContains, region);

            // ∃ sfWithin.Region
            OWLClassExpression someOutgoingToRegion = factory.getOWLObjectSomeValuesFrom(sfDirectlyWithin, region);

            // ¬∃ sfWithin.Region
            OWLClassExpression noOutgoingToRegion = factory.getOWLObjectComplementOf(someOutgoingToRegion);

            // RootRegion ≡ ∀ sfWithin⁻¹.Region ⊓ ∃ sfWithin⁻¹.Region ⊓ ¬∃ sfWithin.Region
            OWLClassExpression rootRegionDefinition = factory.getOWLObjectIntersectionOf(
                    onlyIncomingFromRegions,
                    someIncomingFromRegions,
                    someDirectlyContains
//                    noOutgoingToRegion
            );

            // InternalRegion ≡ ∀ sfWithin⁻¹.Region ⊓ ∃ sfWithin⁻¹.Region ⊓ ∃ sfWithin.Region
            OWLClassExpression internalRegionDefinition = factory.getOWLObjectIntersectionOf(
                    onlyIncomingFromRegions,
                    someIncomingFromRegions,
                    someDirectlyContains
//                    someOutgoingToRegion
            );

            // ParentRegion ≡ (RootRegion ⊔ InternalRegion) ⊓ Region ⊓ AttributesCollection
            OWLClassExpression parentRegionDefinition = factory.getOWLObjectIntersectionOf(
                    factory.getOWLObjectUnionOf(rootRegionDefinition, internalRegionDefinition),
                    region,
                    attributesCollection
            );

            // Axiom: ParentRegion ≡ ...
            destiRecOntology.getManager().addAxiom(
                    destiRecOntology.getOntology(),
                    factory.getOWLEquivalentClassesAxiom(parentRegion, parentRegionDefinition)
            );
        }
    }

    @Override
    public void defineOntology() {
        RegionOntology ontology = new RegionOntology();
        ontology.defineRegion();
        ontology.defineParentRegion();
        ontology.defineLeafRegion();
        ontology.defineRegionParentOrLeaf();

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
