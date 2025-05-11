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
        private final OWLClass region = destiRecOntology.getFactory().getOWLClass(get().stringValue());
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
        }

        private final OWLObjectProperty sfWithin =
                destiRecOntology.getFactory().getOWLObjectProperty(RegionNames.Properties.SF_WITHIN);

        // LeafRegion \equiv AttributesCollection
        // \sqcap \neg \exists sfTransitiveWithin^{-1}.Region \sqcap >=1 \ sfTransitiveWithin.Region
        // \sqcap = 1 \ sfWithin.ParentRegion
        public void defineLeafRegion() {
            OWLObjectProperty sfDirectlyWithin = destiRecOntology.getFactory()
                    .getOWLObjectProperty(RegionNames.Properties.SF_D_WITHIN);

//            OWLObjectPropertyExpression sfContains = destiRecOntology.getFactory().getOWLObjectInverseOf(sfWithin);
            //  \exists sfWithin^{-1}.Region
            OWLClassExpression someSubregionsInRegion = destiRecOntology
                    .getFactory()
                    .getOWLObjectSomeValuesFrom(sfWithin, region);
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

        // ParentRegion \equiv AttributesCollection
        // \sqcap \forall sfWithin^{-1}.Region \sqcap >=1 sfWithin^{-1}.Region \sqcap <=1 sfWithin.Region
        public void defineParentRegion() {
            OWLObjectPropertyExpression sfContains = destiRecOntology.getFactory().getOWLObjectInverseOf(sfWithin);
            // \forall sfWithin^{-1}.Region, all sfWithin contain regions
            OWLClassExpression containsOnlyRegions = destiRecOntology.getFactory().getOWLObjectAllValuesFrom(sfContains, region);
            OWLClassExpression containsMoreThanZero = destiRecOntology
                    .getFactory()
                    .getOWLObjectMinCardinality(1, sfContains, region);
            OWLClassExpression insideOneOrLess = destiRecOntology
                    .getFactory()
                    .getOWLObjectMaxCardinality(1, sfWithin, region);

            OWLClass attributesCollection = destiRecOntology.getFactory()
                    .getOWLClass(AttributeNames.Classes.ATTRIBUTES_COLLECTION.owlIri());

            OWLClassExpression parentDefinition = destiRecOntology
                    .getFactory()
                    .getOWLObjectIntersectionOf(
                            region,
                            containsOnlyRegions,
                            containsMoreThanZero,
                            insideOneOrLess,
                            attributesCollection
                    );

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
