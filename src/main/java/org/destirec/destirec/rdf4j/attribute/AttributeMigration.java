package org.destirec.destirec.rdf4j.attribute;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.destirec.destirec.utils.rdfDictionary.UserNames;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.springframework.stereotype.Component;

import static org.destirec.destirec.utils.rdfDictionary.AttributeNames.Properties.HAS_SCORE;
import static org.destirec.destirec.utils.rdfDictionary.AttributeNames.Properties.IS_ACTIVE;

@Getter
@Component
public class AttributeMigration extends IriMigration implements OntologyDefiner {
    private final org.eclipse.rdf4j.model.IRI hasScore = valueFactory.createIRI(HAS_SCORE.pseudoUri());
    private final org.eclipse.rdf4j.model.IRI isActive = valueFactory.createIRI(IS_ACTIVE.pseudoUri());
    private final DestiRecOntology destiRecOntology;

    protected AttributeMigration(
            RDF4JTemplate rdf4jMethods,
            DestiRecOntology destiRecOntology
    ) {
        super(rdf4jMethods, AttributeNames.Classes.ATTRIBUTE.str());
        this.destiRecOntology = destiRecOntology;
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBPROPERTYOF, TopOntologyNames.Classes.OBJECT);
    }

    @Override
    public void defineOntology() {
        var attributeOntology = new AttributeOntology();
        attributeOntology.defineRegionAttribute();
        attributeOntology.defineScoredAttribute();
        attributeOntology.definePreferenceAttribute();
        attributeOntology.defineUnionOfAttributes();

        var propertiesOntology = new AttributePropertiesOntology();
        propertiesOntology.defineHasAttribute();
        propertiesOntology.defineHasAttributeDescendants();
        propertiesOntology.defineHasConcept();
        propertiesOntology.defineHasConceptAsymmetry();
    }


    class AttributePropertiesOntology {
        private final OWLDataFactory factory = destiRecOntology.getFactory();
        private final OWLOntologyManager manager = destiRecOntology.getManager();

        private final OWLOntology ontology = destiRecOntology.getOntology();

        private final OWLObjectProperty hasConcept = factory
                .getOWLObjectProperty(TopOntologyNames.Properties.HAS_CONCEPT.owlIri());

        private final OWLObjectProperty hasAttribute = factory
                .getOWLObjectProperty(AttributeNames.Properties.HAS_ATTRIBUTE.owlIri());

        private final OWLObjectProperty hasFeature = factory
                .getOWLObjectProperty(AttributeNames.Properties.HAS_FEATURE.owlIri());

        private final OWLObjectProperty hasMonth = factory
                .getOWLObjectProperty(AttributeNames.Properties.HAS_MONTH.owlIri());

        private final OWLObjectProperty hasCost = factory
                .getOWLObjectProperty(AttributeNames.Properties.HAS_COST.owlIri());

        // hasConcept \sqsubseteq (Actor \sqcup Object) \times Concept
        public void defineHasConcept() {
            OWLClass object = factory.getOWLClass(TopOntologyNames.Classes.OBJECT);
            OWLClass actor = factory.getOWLClass(TopOntologyNames.Classes.ACTOR);
            OWLClass concept = factory.getOWLClass(TopOntologyNames.Classes.CONCEPT);

            OWLClassExpression objectOrActor = factory.getOWLObjectUnionOf(object, actor);
            manager.addAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(hasConcept, objectOrActor));

            manager.addAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(hasConcept, concept));
        }

        // hasConcept \sqsubseteq \neg hasConcept^{-1}
        public void defineHasConceptAsymmetry() {
            OWLAxiom disjointInverse = factory.getOWLDisjointObjectPropertiesAxiom(hasConcept, factory.getOWLObjectInverseOf(hasConcept));
            manager.addAxiom(ontology, disjointInverse);
        }

        // hasAttribute \sqsubseteq hasConcept
        public void defineHasAttribute() {
            manager.addAxiom(ontology, factory.getOWLSubObjectPropertyOfAxiom(hasAttribute, hasConcept));
        }

        public void defineHasAttributeDescendants() {
            OWLClass feature = factory.getOWLClass(AttributeNames.Classes.FEATURE.owlIri());
            OWLClass month = factory.getOWLClass(AttributeNames.Classes.MONTH.owlIri());
            OWLClass cost = factory.getOWLClass(AttributeNames.Classes.COST.owlIri());
            OWLClass object = factory.getOWLClass(TopOntologyNames.Classes.OBJECT);
            OWLClass actor = factory.getOWLClass(TopOntologyNames.Classes.ACTOR);

            manager.addAxiom(ontology, factory.getOWLSubObjectPropertyOfAxiom(hasFeature, hasAttribute));
            manager.addAxiom(ontology, factory.getOWLSubObjectPropertyOfAxiom(hasMonth, hasAttribute));
            manager.addAxiom(ontology, factory.getOWLSubObjectPropertyOfAxiom(hasCost, hasAttribute));

            OWLClassExpression objectOrActor = factory.getOWLObjectUnionOf(object, actor);
            manager.addAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(hasFeature, objectOrActor));
            manager.addAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(hasFeature, feature));

            manager.addAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(hasMonth, objectOrActor));
            manager.addAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(hasMonth, month));

            manager.addAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(hasCost, objectOrActor));
            manager.addAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(hasCost, cost));
        }
    }


    class AttributeOntology {
        OWLClass scoredAttribute = destiRecOntology.getFactory()
                .getOWLClass(IRI.create(AttributeNames.Classes.SCORED_ATTRIBUTE.pseudoUri()));
        OWLClass regionAttribute = destiRecOntology.getFactory()
                .getOWLClass(IRI.create(AttributeNames.Classes.REGION_ATTRIBUTE.pseudoUri()));

        OWLClass preferenceAttribute = destiRecOntology.getFactory()
                .getOWLClass(IRI.create(AttributeNames.Classes.PREFERENCE_ATTRIBUTE.pseudoUri()));

        OWLClass region = destiRecOntology.getFactory()
                .getOWLClass(RegionNames.Classes.REGION.pseudoUri());

        OWLClass user = destiRecOntology
                .getFactory()
                .getOWLClass(UserNames.Classes.USER.owlIri());


        OWLClass attribute = destiRecOntology
                .getFactory()
                .getOWLClass(AttributeNames.Classes.ATTRIBUTE.pseudoUri());

        IRI hasScoreOWL = IRI.create(hasScore.stringValue());
        IRI isActiveOWL = IRI.create(isActive.stringValue());

        OWLObjectProperty hasConcept = destiRecOntology.getFactory().getOWLObjectProperty(TopOntologyNames.Properties.HAS_CONCEPT.pseudoUri());

        // ScoredAttribute \equiv Concept \ \sqcap (1= hasScore.Integer) \ \sqcap (1= isActive.Boolean)
        public void defineScoredAttribute() {
            OWLDatatype booleanDatatype = destiRecOntology.getFactory().getOWLDatatype(XSD.BOOLEAN.stringValue());
            OWLDatatype integerDatatype = destiRecOntology.getFactory().getOWLDatatype(XSD.INTEGER.stringValue());

            OWLDataPropertyExpression hasScoreProperty = destiRecOntology.getFactory().getOWLDataProperty(hasScoreOWL);
            OWLClassExpression hasExactlyOneScore = destiRecOntology.getFactory().getOWLDataExactCardinality(1, hasScoreProperty);
            OWLLiteral minScoreValue = destiRecOntology.getFactory().getOWLLiteral(0);
            OWLLiteral maxScoreValue = destiRecOntology.getFactory().getOWLLiteral(100);

            OWLFacetRestriction minRestriction = destiRecOntology.getFactory().getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, minScoreValue);
            OWLFacetRestriction maxRestriction = destiRecOntology.getFactory().getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, maxScoreValue);

            OWLDatatypeRestriction scoreRangeType = destiRecOntology.getFactory().getOWLDatatypeRestriction(
                    integerDatatype,
                    minRestriction,
                    maxRestriction
            );

            OWLDataPropertyRangeAxiom rangeAxiomForScore = destiRecOntology.getFactory()
                    .getOWLDataPropertyRangeAxiom(
                            hasScoreProperty,
                            scoreRangeType
                    );

            OWLDataProperty isActiveProperty = destiRecOntology.getFactory().getOWLDataProperty(isActiveOWL);
            OWLClassExpression isExactlyOneActive = destiRecOntology.getFactory().getOWLDataExactCardinality(1, isActiveProperty);
            OWLDataAllValuesFrom isActiveBoolean = destiRecOntology.getFactory().getOWLDataAllValuesFrom(isActiveProperty, booleanDatatype);

            OWLClassExpression intersectionScoredAttribute = destiRecOntology.getFactory().getOWLObjectIntersectionOf(
                    destiRecOntology.getFactory().getOWLClass(TopOntologyNames.Classes.CONCEPT),
                    hasExactlyOneScore,
                    isActiveBoolean,
                    isExactlyOneActive
            );
            destiRecOntology.getManager()
                    .addAxiom(
                            destiRecOntology.getOntology(),
                            destiRecOntology.getFactory().getOWLEquivalentClassesAxiom(
                                    scoredAttribute,
                                    intersectionScoredAttribute
                            )
                    );

            destiRecOntology.getManager()
                    .addAxiom(getDestiRecOntology().getOntology(), rangeAxiomForScore);
        }

        public void defineRegionAttribute() {
            var hasConceptInv = destiRecOntology.getFactory().getOWLObjectInverseOf(hasConcept);
            OWLClassExpression hasInvConceptClass = destiRecOntology.getFactory()
                    .getOWLObjectSomeValuesFrom(hasConceptInv, region);

            OWLClassExpression intersectionWithScoredAttr = destiRecOntology.getFactory()
                    .getOWLObjectIntersectionOf(scoredAttribute, hasInvConceptClass);
            destiRecOntology
                    .getManager()
                    .addAxiom(
                            destiRecOntology.getOntology(),
                            destiRecOntology.getFactory().getOWLEquivalentClassesAxiom(
                                    regionAttribute,
                                    intersectionWithScoredAttr
                            )
                    );
        }


        public void definePreferenceAttribute() {
            var hasConceptInv = destiRecOntology.getFactory().getOWLObjectInverseOf(hasConcept);
            OWLClassExpression hasInvConceptClass = destiRecOntology.getFactory()
                    .getOWLObjectSomeValuesFrom(hasConceptInv, user);

            OWLClassExpression intersectionWithScoredAttr = destiRecOntology.getFactory()
                    .getOWLObjectIntersectionOf(scoredAttribute, hasInvConceptClass);
            destiRecOntology
                    .getManager()
                    .addAxiom(
                            destiRecOntology.getOntology(),
                            destiRecOntology.getFactory().getOWLEquivalentClassesAxiom(
                                    preferenceAttribute,
                                    intersectionWithScoredAttr
                            )
                    );
        }


        // Attribute \equiv  RegionAttribute \ \sqcup  PreferenceAttribute
        // there cannot be other types of attributes: region or preference only
        public void defineUnionOfAttributes() {
            destiRecOntology.getManager()
                    .addAxiom(
                            destiRecOntology.getOntology(),
                            destiRecOntology.getFactory().getOWLEquivalentClassesAxiom(
                                    attribute,
                                    destiRecOntology.getFactory().getOWLObjectUnionOf(regionAttribute, preferenceAttribute)
                            )
                    );
        }
    }
}
