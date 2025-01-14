package org.destirec.destirec.rdf4j.months;


import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.Migration;
import org.destirec.destirec.rdf4j.interfaces.PredicateInstance;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

@Component
@Getter
public class MonthMigration extends Migration {
    protected PredicateInstance month;
    protected PredicateInstance valueRange;

    public MonthMigration(RDF4JTemplate rdf4jMethods) {
        super(rdf4jMethods, "Month");
        initMonth();;
    }

    protected void initMonth() {
        month = new PredicateInstance(
                rdf4jMethods, "month",
                instance -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.COMMENT, "month of the year")
                        .add(instance.predicate(), RDFS.RANGE, XSD.GMONTH)
        );

        valueRange = new PredicateInstance(
                rdf4jMethods, "monthRange",
                instance -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.COMMENT, "range of the month")
                        .add(instance.predicate(), RDFS.RANGE, XSD.FLOAT)
        );
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(get(), RDFS.SUBPROPERTYOF, WIKIDATA.PREFERENCE)
                .add(get(), RDFS.RANGE, RDFS.RESOURCE);
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