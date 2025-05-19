package org.destirec.destirec.rdf4j.region;

import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.semanticweb.owlapi.model.*;

class RegionPropertiesOntology {
    private final OWLDataFactory factory;
    private final OWLOntologyManager manager;
    private final OWLOntology ontology;

    private final OWLClass regionLike;
    private final OWLObjectProperty sfWithin;
    private final OWLObjectProperty sfContains;
    private final OWLObjectProperty sfDirectlyWithin;
    private final OWLObjectProperty sfDirectlyContains;

    RegionPropertiesOntology(OWLDataFactory factory, OWLOntologyManager manager, OWLOntology ontology) {
        this.factory = factory;
        this.manager = manager;
        this.ontology = ontology;

        this.sfWithin = factory.getOWLObjectProperty(RegionNames.Properties.SF_WITHIN);
        this.sfContains = factory.getOWLObjectProperty(RegionNames.Properties.SF_CONTAINS);
        this.sfDirectlyWithin = factory.getOWLObjectProperty(RegionNames.Properties.SF_D_WITHIN);
        this.sfDirectlyContains = factory.getOWLObjectProperty(RegionNames.Properties.SF_D_CONTAINS);
        this.regionLike = factory.getOWLClass(RegionNames.Classes.REGION_LIKE.owlIri());
    }


    // sfWithin ⊑ Region×Region
    public void defineSfWithinMapping() {
        manager.addAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(sfWithin, regionLike));
        manager.addAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(sfWithin, regionLike));
    }

    // sfWithin^−1≡sfContains
    public void defineSfWithinOpposite() {
        OWLAxiom inverseSfWithin = factory.getOWLInverseObjectPropertiesAxiom(sfWithin, sfContains);
        manager.addAxiom(ontology, inverseSfWithin);
    }

    // sfDirectlyWithin
    public void defineSfDirectlyWithin() {
        manager.addAxiom(ontology, factory.getOWLSubObjectPropertyOfAxiom(sfDirectlyWithin, sfWithin));
        manager.addAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(sfDirectlyWithin, regionLike));
        manager.addAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(sfDirectlyWithin, regionLike));
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
