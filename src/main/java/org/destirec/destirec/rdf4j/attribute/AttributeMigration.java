package org.destirec.destirec.rdf4j.attribute;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
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
    private final org.eclipse.rdf4j.model.IRI hasScore = valueFactory.createIRI(HAS_SCORE);
    private final org.eclipse.rdf4j.model.IRI isActive = valueFactory.createIRI(IS_ACTIVE);
    private final DestiRecOntology destiRecOntology;

    protected AttributeMigration(
            RDF4JTemplate rdf4jMethods,
            DestiRecOntology destiRecOntology
    ) {
        super(rdf4jMethods, "Attribute");
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
    }


    class AttributeOntology {
        OWLClass scoredAttribute = destiRecOntology.getFactory()
                .getOWLClass(IRI.create(AttributeNames.Classes.SCORED_ATTRIBUTE));
        OWLClass regionAttribute = destiRecOntology.getFactory()
                .getOWLClass(IRI.create(AttributeNames.Classes.REGION_ATTRIBUTE));

        OWLClass preferenceAttribute = destiRecOntology.getFactory()
                .getOWLClass(IRI.create(AttributeNames.Classes.PREFERENCE_ATTRIBUTE));

        OWLClass region = destiRecOntology.getFactory()
                .getOWLClass(RegionNames.Classes.REGION);

        OWLClass user = destiRecOntology
                .getFactory()
                .getOWLClass(UserNames.Classes.USER);


        OWLClass attribute = destiRecOntology
                .getFactory()
                .getOWLClass(AttributeNames.Classes.ATTRIBUTE);

        IRI hasScoreOWL = IRI.create(hasScore.stringValue());
        IRI isActiveOWL = IRI.create(isActive.stringValue());

        OWLObjectProperty hasConcept = destiRecOntology.getFactory().getOWLObjectProperty(DESTIREC.wrapNamespace("hasConcept"));

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
