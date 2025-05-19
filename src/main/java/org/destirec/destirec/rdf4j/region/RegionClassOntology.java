package org.destirec.destirec.rdf4j.region;

import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.semanticweb.owlapi.model.*;

class RegionClassOntology {
    private final OWLDataFactory factory;
    private final OWLOntologyManager manager;
    private final OWLOntology ontology;
    private final OWLClass region;

    private final OWLClass regionLike;
    private final OWLClass object;
    private final OWLClass parentRegion;
    private final OWLClass leafRegion;
    private final OWLClass containsRegion;

    private final OWLClass noRegion;
    private final OWLClass insideRegion;

    private final OWLClass rootRegion;

    private final OWLObjectProperty sfDirectlyWithin;
    private final OWLObjectProperty sfDirectlyContains;
    private final OWLObjectProperty sfTransitiveWithin;
    private final OWLObjectProperty sfTransitiveContains;;

    RegionClassOntology(OWLDataFactory factory, OWLOntologyManager manager, OWLOntology ontology) {
        this.factory = factory;
        this.manager = manager;
        this.ontology = ontology;

        this.region = factory.getOWLClass(RegionNames.Classes.REGION.owlIri());
        this.object = factory.getOWLClass(TopOntologyNames.Classes.OBJECT.owlIri());

        this.parentRegion = factory.getOWLClass(RegionNames.Classes.PARENT_REGION.owlIri());
        this.leafRegion = factory.getOWLClass(RegionNames.Classes.LEAF_REGION.owlIri());

        this.containsRegion = factory.getOWLClass(RegionNames.Classes.CONTAINS_REGION.owlIri());
        this.insideRegion = factory.getOWLClass(RegionNames.Classes.INSIDE_REGION.owlIri());
        this.rootRegion = factory.getOWLClass(RegionNames.Classes.ROOT_REGION.owlIri());

        this.noRegion = factory.getOWLClass(RegionNames.Classes.NO_REGION.owlIri());

        this.sfDirectlyWithin = factory.getOWLObjectProperty(RegionNames.Properties.SF_D_WITHIN);
        this.sfDirectlyContains = factory.getOWLObjectProperty(RegionNames.Properties.SF_D_CONTAINS);

        this.sfTransitiveWithin = factory.getOWLObjectProperty(RegionNames.Properties.SF_WITHIN);
        this.sfTransitiveContains = factory.getOWLObjectProperty(RegionNames.Properties.SF_CONTAINS);

        this.regionLike = factory.getOWLClass(RegionNames.Classes.REGION_LIKE.owlIri());
    }

    public void defineRegion() {
        // Region \sqsubseteq Object, region is subclass of object
        manager.addAxiom(
                ontology,
                factory.getOWLSubClassOfAxiom(regionLike, object)
        );

        manager.addAxiom(
                ontology,
                factory.getOWLSubClassOfAxiom(region, regionLike)
        );
    }


    public void defineEmptyRegion() {
        manager.addAxiom(
                ontology, factory.getOWLSubClassOfAxiom(noRegion, regionLike)
        );

        OWLIndividual noRegionInstance = factory.getOWLNamedIndividual(RegionNames.Individuals.NO_REGION.owlIri());
        manager.addAxiom(
                ontology, factory.getOWLClassAssertionAxiom(noRegion, noRegionInstance)
        );

        OWLClassExpression expression = factory.getOWLObjectAllValuesFrom(sfDirectlyContains,
                factory.getOWLClass(OWL.NOTHING.stringValue()));

        manager.addAxiom(
                ontology, factory.getOWLEquivalentClassesAxiom(noRegion, expression)
        );
    }

    public void defineInsideRegion() {
        manager.addAxiom(
                ontology,
                factory.getOWLSubClassOfAxiom(insideRegion, region)
        );

        OWLClassExpression insideOne = factory.getOWLObjectExactCardinality(1, sfDirectlyWithin, region);
        OWLClassExpression insideMany = factory.getOWLObjectSomeValuesFrom(sfTransitiveWithin, region);
        OWLClassExpression allRegions = factory.getOWLObjectAllValuesFrom(sfDirectlyWithin, region);

        manager.addAxiom(
                ontology,
                factory.getOWLEquivalentClassesAxiom(
                        insideRegion,
                        factory.getOWLObjectIntersectionOf(insideOne, allRegions, insideMany)
                )
        );
    }

    public void defineContainsRegion() {
        manager.addAxiom(
                ontology,
                factory.getOWLSubClassOfAxiom(containsRegion, region)
        );

        OWLClassExpression containsOne = factory.getOWLObjectSomeValuesFrom(sfDirectlyContains, region);
        OWLClassExpression containsMore = factory.getOWLObjectSomeValuesFrom(sfTransitiveContains, region);

        manager.addAxiom(
                ontology,
                factory.getOWLEquivalentClassesAxiom(
                        containsRegion,
                        factory.getOWLObjectIntersectionOf(containsOne, containsMore)
//                        factory.getOWLObjectUnionOf(factory.getOWLObjectIntersectionOf(containsOne, containsMore), insideRegion)
                )
        );
    }


    public void defineLeafRegion() {
        manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(leafRegion, region));
//            OWLObjectPropertyExpression sfContains = destiRecOntology.getFactory().getOWLObjectInverseOf(sfWithin);
        //  \exists sfWithin^{-1}.NoRegion
        OWLClassExpression subregionsAreNothing = factory.getOWLObjectAllValuesFrom(sfDirectlyContains, noRegion);
        OWLClassExpression subregionsAreOne = factory.getOWLObjectExactCardinality(1, sfDirectlyContains, noRegion);

//        // (>1 \ sfWithin.Region)
        OWLClassExpression insideOneRegionDirectly = factory
                .getOWLObjectExactCardinality(1, sfDirectlyWithin, region);

        OWLClassExpression onlyInsideRegion = factory.getOWLObjectAllValuesFrom(sfDirectlyWithin, region);

        // Define LeafRegion
        manager
                .addAxiom(
                        ontology,
                        factory.getOWLEquivalentClassesAxiom(leafRegion,
//                                subregionsAreNothing
                                factory.getOWLObjectIntersectionOf(
                                        subregionsAreNothing,
                                        subregionsAreOne,
                                        insideOneRegionDirectly,
                                        insideOneRegionDirectly
//                                        onlyInsideRegion
                                ))
                        );
//                );
    }

    public void defineRoot() {
        manager.addAxiom(
                ontology,
                factory.getOWLSubClassOfAxiom(rootRegion, region)
        );

        OWLObjectComplementOf noWithin = factory.getOWLObjectComplementOf(factory
                .getOWLObjectSomeValuesFrom(sfTransitiveContains, rootRegion));


        manager.addAxiom(
                ontology,
                factory.getOWLEquivalentClassesAxiom(
                        rootRegion,
                        noWithin
                )
        );
    }

    // ParentRegion ≡ InternalRegion ⊔ RootRegion
    // RootRegion ≡ ∀ sfWithin⁻¹.Region ⊓ ∃ sfWithin⁻¹.Region ⊓ ¬∃ sfWithin.Region
    // InternalRegion ≡ ∀ sfWithin⁻¹.Region ⊓ ∃ sfWithin⁻¹.Region ⊓ ∃ sfWithin.Region
    // RootRegion ⊓ InternalRegion ⊑ ⊥
    public void defineParentRegion() {
        // ∃ sfDirectlyContains⁻¹.Region
        OWLClassExpression someDirectlyContains = factory.getOWLObjectMinCardinality(1, sfDirectlyContains, region);

        OWLClassExpression oneDWithin = factory.getOWLObjectExactCardinality(1, sfDirectlyWithin, region);

        // ParentRegion ≡ (RootRegion ⊔ InternalRegion) ⊓ Region ⊓ AttributesCollection
        OWLClassExpression parentRegionDefinition = factory
                .getOWLObjectIntersectionOf(someDirectlyContains, oneDWithin);

        // Axiom: ParentRegion ≡ ...
        manager.addAxiom(
                ontology,
                factory.getOWLEquivalentClassesAxiom(parentRegion, parentRegionDefinition)
        );
    }
}