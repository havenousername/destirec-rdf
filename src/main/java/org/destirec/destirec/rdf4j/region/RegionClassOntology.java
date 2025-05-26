package org.destirec.destirec.rdf4j.region;

import org.destirec.destirec.rdf4j.ontology.AppOntology;
import org.destirec.destirec.utils.rdfDictionary.POINames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.semanticweb.owlapi.model.*;

class RegionClassOntology {
    private final OWLDataFactory factory;
    private final OWLClass region;

    private final OWLClass regionLike;
    private final OWLClassExpression object;
    private final OWLClass parentRegion;
    private final OWLClass leafRegion;
    private final OWLClass containsRegion;

    private final OWLClass noRegion;
    private final OWLClass insideRegion;

    private final OWLClass rootRegion;

    private final OWLObjectProperty sfDirectlyWithin;
    private final OWLObjectProperty sfDirectlyContains;
    private final OWLObjectProperty sfTransitiveWithin;
    private final OWLObjectProperty sfTransitiveContains;
    private final AppOntology ontology;
    private final OWLClass poi;


    RegionClassOntology(AppOntology ontology, OWLDataFactory factory) {
        this.factory = factory;
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
        this.poi = factory.getOWLClass(POINames.Classes.POI.owlIri());
    }

    public void defineRegion() {
        // Region \sqsubseteq Object, region is subclass of object
//        ontology.addAxiom(factory.getOWLEquivalentClassesAxiom(region, factory.getOWLObjectIntersectionOf(attributeSelection, object)));
        ontology.addAxiom(
                factory.getOWLSubClassOfAxiom(regionLike, object)
        );

        ontology.addAxiom(
                factory.getOWLSubClassOfAxiom(region, regionLike)
        );

        ontology.addAxiom(
                factory.getOWLDisjointClassesAxiom(leafRegion, parentRegion));
    }


    public void defineEmptyRegion() {
        OWLIndividual noRegionInstance = factory.getOWLNamedIndividual(RegionNames.Individuals.NO_REGION.owlIri());
        ontology.addAxiom( factory.getOWLClassAssertionAxiom(noRegion, noRegionInstance)
        );
    }

    public void defineInsideRegion() {
        ontology.addAxiom(
                factory.getOWLSubClassOfAxiom(insideRegion, region)
        );

        OWLClassExpression insideOne = factory.getOWLObjectExactCardinality(1, sfDirectlyWithin, region);
        OWLClassExpression insideMany = factory.getOWLObjectSomeValuesFrom(sfTransitiveWithin, region);
        OWLClassExpression allRegions = factory.getOWLObjectAllValuesFrom(sfDirectlyWithin, region);

        ontology.addAxiom(
                factory.getOWLEquivalentClassesAxiom(
                        insideRegion,
                        factory.getOWLObjectIntersectionOf(insideOne, allRegions, insideMany)
                )
        );
    }

    public void defineContainsRegion() {
        ontology.addAxiom(
                factory.getOWLSubClassOfAxiom(containsRegion, region)
        );

        OWLClassExpression containsOne = factory.getOWLObjectSomeValuesFrom(sfDirectlyContains, region);
        OWLClassExpression containsMore = factory.getOWLObjectSomeValuesFrom(sfTransitiveContains, region);

        ontology.addAxiom(
                factory.getOWLEquivalentClassesAxiom(
                        containsRegion,
                        factory.getOWLObjectIntersectionOf(containsOne, containsMore)
//                        factory.getOWLObjectUnionOf(factory.getOWLObjectIntersectionOf(containsOne, containsMore), insideRegion)
                )
        );
    }


    public void defineLeafRegion() {
        ontology.addAxiom(factory.getOWLSubClassOfAxiom(leafRegion, region));
//            OWLObjectPropertyExpression sfContains = destiRecOntology.getFactory().getOWLObjectInverseOf(sfWithin);
        //  \exists sfWithin^{-1}.NoRegion
//        var directlyContains = factory.getOWLObjectProperty(RegionNames.Properties.CONTAINS_EMPTY.owlIri());
        OWLClassExpression subregionsAreOne = factory.getOWLObjectSomeValuesFrom(sfDirectlyContains, poi);

//        // (=1 \ sfWithin.Region)
        OWLClassExpression insideOneRegionDirectly = factory
                .getOWLObjectSomeValuesFrom(sfDirectlyWithin, region);

        // Define LeafRegion
        ontology
                .addAxiom(
                        factory.getOWLEquivalentClassesAxiom(leafRegion,
                                factory.getOWLObjectIntersectionOf(
//                                        subregionsAreNothing,
                                        subregionsAreOne,
                                        insideOneRegionDirectly
//                                        onlyInsideRegion
                                ))
                        );
//                );
    }

    public void defineRoot() {
        ontology.addAxiom(
                factory.getOWLSubClassOfAxiom(rootRegion, region)
        );

        OWLObjectComplementOf noWithin = factory.getOWLObjectComplementOf(factory
                .getOWLObjectSomeValuesFrom(sfTransitiveContains, rootRegion));


        ontology.addAxiom(
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
        ontology.addAxiom(factory.getOWLSubClassOfAxiom(parentRegion, region));
        // ∃ sfDirectlyContains⁻¹.Region
        OWLClassExpression someDirectlyContains = factory.getOWLObjectSomeValuesFrom(sfDirectlyContains, region);

        // ParentRegion ≡ (RootRegion ⊔ InternalRegion) ⊓ Region ⊓ AttributesCollection
        OWLClassExpression parentRegionDefinition = factory
                .getOWLObjectIntersectionOf(someDirectlyContains, region);

        // Axiom: ParentRegion ≡ ...
        ontology.addAxiom(
                factory.getOWLEquivalentClassesAxiom(parentRegion, parentRegionDefinition)
        );
    }
}