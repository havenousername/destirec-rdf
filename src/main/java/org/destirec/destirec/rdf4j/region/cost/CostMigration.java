package org.destirec.destirec.rdf4j.region.cost;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.IriMigrationInstance;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.vocabulary.DBPEDIA;
import org.destirec.destirec.rdf4j.vocabulary.SCHEMA;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Component;

@Component
@Getter
public class CostMigration extends IriMigration implements OntologyDefiner {
    private IriMigrationInstance costPerWeekPredicate;
    private IriMigrationInstance budgetLevelPredicate;
    private final DestiRecOntology destiRecOntology;
    public CostMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology destiRecOntology) {
        super(rdf4jMethods,  AttributeNames.Classes.COST.str());
        this.destiRecOntology = destiRecOntology;
        initBudgetLevelPredicate();
        initCostPerWeekPredicate();
    }

    protected void initCostPerWeekPredicate() {
        costPerWeekPredicate = new IriMigrationInstance(
                rdf4jMethods, AttributeNames.Properties.HAS_SCORE_PER_WEEK.str(),
                instance -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.SUBPROPERTYOF, SCHEMA.PRICE_SPECIFICATION)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.RANGE, XSD.INTEGER)
                        .add(instance.predicate(), RDFS.COMMENT, "shows cost per week of the entity")
        );
    }

    protected void initBudgetLevelPredicate() {
        budgetLevelPredicate = new IriMigrationInstance(
                rdf4jMethods, AttributeNames.Properties.HAS_BUDGET_LEVEL.str(),
                instance -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.RANGE, XSD.INTEGER)
        );
    }

    @Override
    public void defineOntology() {
        var budgetOntology = new BudgetOntology();
        budgetOntology.defineCost();
    }

    class BudgetOntology {
        OWLClass attribute = destiRecOntology
                .getFactory()
                .getOWLClass(AttributeNames.Classes.ATTRIBUTE.pseudoUri());
        OWLClass cost = destiRecOntology
                .getFactory()
                .getOWLClass(AttributeNames.Classes.COST.pseudoUri());

       //  Cost \equiv Attribute \ \sqcap (\exists hasBudgetLevel.Integer) \ \sqcap (=1 \  hasCostPerWeek.Integer)
       public void defineCost() {
           OWLDataPropertyExpression hasCostPerWeek = destiRecOntology
                   .getFactory()
                   .getOWLDataProperty(AttributeNames.Properties.HAS_SCORE_PER_WEEK.pseudoUri());

           OWLDataProperty hasBudgetLevel = destiRecOntology
                   .getFactory()
                   .getOWLDataProperty(AttributeNames.Properties.HAS_BUDGET_LEVEL.pseudoUri());

           OWLDatatype integerDatatype = destiRecOntology.getFactory().getOWLDatatype(XSD.INTEGER.stringValue());
           var existsCostPerWeek = destiRecOntology.getFactory().getOWLDataAllValuesFrom(hasCostPerWeek, integerDatatype);
           OWLClassExpression hasExactlyOneCostPerWeek = destiRecOntology
                   .getFactory()
                   .getOWLDataExactCardinality(1, hasCostPerWeek);
           var existsBudgetLevel = destiRecOntology.getFactory().getOWLDataSomeValuesFrom(hasBudgetLevel, integerDatatype);

           OWLClassExpression intersectionScoredAttribute = destiRecOntology.getFactory().getOWLObjectIntersectionOf(
                   attribute,
                   existsBudgetLevel,
                   hasExactlyOneCostPerWeek,
                   existsCostPerWeek
           );


           destiRecOntology.getManager()
                   .addAxiom(
                           destiRecOntology.getOntology(),
                           destiRecOntology.getFactory().getOWLEquivalentClassesAxiom(
                                   cost,
                                   intersectionScoredAttribute
                           )
                   );
       }
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(get(), RDFS.SUBCLASSOF, TopOntologyNames.Classes.CONCEPT.rdfIri())
                .add(get(), SKOS.RELATED_MATCH, DBPEDIA.COST)
                .add(get(), RDFS.RANGE, RDFS.RESOURCE);
    }

    @Override
    public void setup() {
        super.setup();
        budgetLevelPredicate.setup();
        costPerWeekPredicate.setup();
    }

    @Override
    public void migrate() {
        super.migrate();
        costPerWeekPredicate.migrate();
        budgetLevelPredicate.migrate();
    }
}
