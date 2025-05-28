package org.destirec.destirec.rdf4j.region;

import org.destirec.destirec.rdf4j.ontology.AppOntology;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.semanticweb.owlapi.model.*;

class RegionPropertiesOntology {
    private final OWLDataFactory factory;
    private final AppOntology ontology;

    private final OWLClass regionLike;
    private final OWLObjectProperty sfWithin;
    private final OWLObjectProperty sfContains;
    private final OWLObjectProperty sfDirectlyWithin;
    private final OWLObjectProperty sfDirectlyContains;

    RegionPropertiesOntology(AppOntology ontology, OWLDataFactory factory) {
        this.factory = factory;
        this.ontology = ontology;

        this.sfWithin = factory.getOWLObjectProperty(RegionNames.Properties.SF_WITHIN);
        this.sfContains = factory.getOWLObjectProperty(RegionNames.Properties.SF_CONTAINS);
        this.sfDirectlyWithin = factory.getOWLObjectProperty(RegionNames.Properties.SF_D_WITHIN);
        this.sfDirectlyContains = factory.getOWLObjectProperty(RegionNames.Properties.SF_D_CONTAINS);
        this.regionLike = factory.getOWLClass(RegionNames.Classes.REGION_LIKE.owlIri());
    }


    // sfWithin ⊑ Region×Region
    public void defineSfWithinMapping() {
        ontology.addAxiom(factory.getOWLObjectPropertyDomainAxiom(sfWithin, regionLike));
        ontology.addAxiom(factory.getOWLObjectPropertyRangeAxiom(sfWithin, regionLike));
    }

    // sfWithin^−1≡sfContains
    public void defineSfWithinOpposite() {
        OWLAxiom inverseSfWithin = factory.getOWLInverseObjectPropertiesAxiom(sfWithin, sfContains);
        ontology.addAxiom(inverseSfWithin);
    }

    // sfDirectlyWithin
    public void defineSfDirectlyWithin() {
        ontology.addAxiom( factory.getOWLSubObjectPropertyOfAxiom(sfDirectlyWithin, sfWithin));
        ontology.addAxiom(factory.getOWLObjectPropertyDomainAxiom(sfDirectlyWithin, regionLike));
        ontology.addAxiom(factory.getOWLObjectPropertyRangeAxiom(sfDirectlyWithin, regionLike));
        // ≤1sfDirectlyWithin
        ontology.addAxiom(factory.getOWLFunctionalObjectPropertyAxiom(sfDirectlyWithin));

        OWLAxiom inverseAxiom = factory.getOWLInverseObjectPropertiesAxiom(sfDirectlyContains, sfDirectlyWithin);
        ontology.addAxiom(inverseAxiom);
    }


    // sfWithin⊑+sfWithin
    public void defineSfWithinTransitive() {
        ontology.addAxiom(factory.getOWLTransitiveObjectPropertyAxiom(sfWithin));
    }

    //        // ∀x.¬sfWithin(x,x) - irreflexive, region cannot contain itself - Germany cannot contain Germany
    public void defineSfWithinIrreflexive(){
        ontology.addAxiom(factory.getOWLIrreflexiveObjectPropertyAxiom(sfDirectlyWithin));
    }

    // sfWithin ⊑ ¬sfWithin⁻¹
    public void defineDisjointAsymmetry() {
        OWLAxiom disjointInverse = factory.getOWLDisjointObjectPropertiesAxiom(sfDirectlyWithin, factory.getOWLObjectInverseOf(sfDirectlyWithin));
        ontology.addAxiom(disjointInverse);
    }
}
