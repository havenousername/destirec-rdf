package org.destirec.destirec.rdf4j.months;


import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.IriMigrationInstance;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.springframework.stereotype.Component;

import static org.destirec.destirec.utils.rdfDictionary.AttributeNames.Properties.*;

@Component
@Getter
public class MonthMigration extends IriMigration implements OntologyDefiner {
    protected IriMigrationInstance monthInstance;
    protected IriMigrationInstance valueRangeInstance;
    private final org.eclipse.rdf4j.model.IRI hasScore = valueFactory.createIRI(HAS_SCORE.pseudoUri());
    private final org.eclipse.rdf4j.model.IRI isActive = valueFactory.createIRI(IS_ACTIVE.pseudoUri());
    private final org.eclipse.rdf4j.model.IRI monthName = valueFactory.createIRI(NAME.pseudoUri());
    private final org.eclipse.rdf4j.model.IRI position = valueFactory.createIRI(POSITION.pseudoUri());
    private final org.eclipse.rdf4j.model.IRI next = valueFactory.createIRI(NEXT.pseudoUri());
    private final DestiRecOntology destiRecOntology;
    protected IriMigrationInstance month;
    protected IriMigrationInstance valueRange;

    public MonthMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology destiRecOntology) {
        super(rdf4jMethods, AttributeNames.Classes.MONTH.str());
        this.destiRecOntology = destiRecOntology;
        initMonth();;
    }

    protected void initMonth() {
        month = new IriMigrationInstance(
                rdf4jMethods, NAME.str(),
                instance -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.COMMENT, "month of the year")
                        .add(instance.predicate(), RDFS.RANGE, XSD.STRING)
        );

        valueRange = new IriMigrationInstance(
                rdf4jMethods, POSITION.str(),
                instance -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.COMMENT, "index of the month in the calendar")
                        .add(instance.predicate(), RDFS.RANGE, XSD.INTEGER)
        );
    }

    @Override
    public void defineOntology() {
        var monthOntology = new MonthOntology();
        monthOntology.defineMonthAttribute();
    }

    class MonthOntology {
        OWLClass attribute = destiRecOntology
                .getFactory()
                .getOWLClass(AttributeNames.Classes.ATTRIBUTE.owlIri());
        OWLClass month = destiRecOntology
                .getFactory()
                .getOWLClass(AttributeNames.Classes.MONTH.owlIri());


        private OWLDatatypeRestriction createMonthDataRange() {
            OWLLiteral minScoreValue = destiRecOntology.getFactory().getOWLLiteral(1);
            OWLLiteral maxScoreValue = destiRecOntology.getFactory().getOWLLiteral(12);

            OWLFacetRestriction minRestriction = destiRecOntology.getFactory().getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, minScoreValue);
            OWLFacetRestriction maxRestriction = destiRecOntology.getFactory().getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, maxScoreValue);

            OWLDatatype integerDatatype = destiRecOntology.getFactory().getOWLDatatype(XSD.INTEGER.stringValue());
            return destiRecOntology.getFactory().getOWLDatatypeRestriction(
                    integerDatatype,
                    minRestriction,
                    maxRestriction
            );
        }

        // Month \equiv Attribute \ \sqcap name.String \sqcap position.Integer \ \sqcap next.Month \text{ where } > hasScore.Month \text{ then current}
        public void defineMonthAttribute() {
            OWLDatatype stringDatatype = destiRecOntology.getFactory().getOWLDatatype(XSD.STRING.stringValue());

            OWLDataProperty hasNameProperty = destiRecOntology.getFactory().getOWLDataProperty(monthName.stringValue());
            OWLClassExpression hasExactlyOneName = destiRecOntology
                    .getFactory()
                    .getOWLDataExactCardinality(1, hasNameProperty);
            OWLDataAllValuesFrom allNameString = destiRecOntology.getFactory().getOWLDataAllValuesFrom(hasNameProperty, stringDatatype);

            OWLObjectPropertyExpression hasNextProperty = destiRecOntology.getFactory().getOWLObjectProperty(next.stringValue());
            OWLClassExpression hasExactlyOneNext = destiRecOntology
                    .getFactory()
                    .getOWLObjectExactCardinality(1, hasNextProperty);
            OWLClassExpression nextIsMonth = destiRecOntology.getFactory().getOWLObjectAllValuesFrom(
                    hasNextProperty,
                    month
            );

            OWLDataProperty hasPositionProperty = destiRecOntology.getFactory().getOWLDataProperty(position.stringValue());
            OWLClassExpression hasExactlyOnePositionProperty = destiRecOntology
                    .getFactory()
                    .getOWLDataExactCardinality(1, hasPositionProperty);

            OWLDataPropertyRangeAxiom rangeAxiomForMonthPosition = destiRecOntology.getFactory()
                    .getOWLDataPropertyRangeAxiom(
                            hasPositionProperty,
                            createMonthDataRange()
                    );

            destiRecOntology
                    .getManager()
                    .addAxiom(destiRecOntology.getOntology(), rangeAxiomForMonthPosition);

            OWLClassExpression intersectionScoredAttribute = destiRecOntology.getFactory().getOWLObjectIntersectionOf(
                    attribute,
                    hasExactlyOneName,
                    hasExactlyOneNext,
                    nextIsMonth,
                    hasExactlyOnePositionProperty,
                    allNameString
            );

            destiRecOntology.getManager()
                    .addAxiom(
                            destiRecOntology.getOntology(),
                            destiRecOntology.getFactory().getOWLEquivalentClassesAxiom(
                                    month,
                                    intersectionScoredAttribute
                            )
                    );
        }
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, TopOntologyNames.Classes.CONCEPT);
    }


    @Override
    public void setup() {
        super.setup();
        valueRange.setup();
        month.setup();
    }

    @Override
    public void migrate() {
        super.migrate();
        valueRange.migrate();
        month.migrate();
    }
}