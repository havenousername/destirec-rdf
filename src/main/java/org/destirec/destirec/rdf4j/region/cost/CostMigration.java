package org.destirec.destirec.rdf4j.region.cost;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.Migration;
import org.destirec.destirec.rdf4j.interfaces.PredicateInstance;
import org.destirec.destirec.rdf4j.vocabulary.DBPEDIA;
import org.destirec.destirec.rdf4j.vocabulary.SCHEMA;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

@Component
@Getter
public class CostMigration extends Migration {
    private PredicateInstance costPerWeekPredicate;
    private PredicateInstance budgetLevelPredicate;
    public CostMigration(RDF4JTemplate rdf4jMethods) {
        super(rdf4jMethods, "Cost");
        initBudgetLevelPredicate();
        initCostPerWeekPredicate();
    }

    protected void initCostPerWeekPredicate() {
        costPerWeekPredicate = new PredicateInstance(
                rdf4jMethods, "hasCostPerWeek",
                instance -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.SUBPROPERTYOF, SCHEMA.PRICE_SPECIFICATION)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.RANGE, XSD.FLOAT)
                        .add(instance.predicate(), RDFS.COMMENT, "shows cost per week of the entity")
        );
    }

    protected void initBudgetLevelPredicate() {
        budgetLevelPredicate = new PredicateInstance(
                rdf4jMethods, "hasBudgetLevel",
                instance -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.RANGE, XSD.FLOAT)
        );
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(get(), RDFS.SUBPROPERTYOF, DBPEDIA.COST)
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
