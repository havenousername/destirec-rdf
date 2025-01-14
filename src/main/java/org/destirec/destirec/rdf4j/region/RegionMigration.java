package org.destirec.destirec.rdf4j.region;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.Migration;
import org.destirec.destirec.rdf4j.interfaces.PredicateInstance;
import org.destirec.destirec.rdf4j.vocabulary.DBPEDIA;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

@Component
@Getter
public class RegionMigration extends Migration {
    private PredicateInstance hasCost;
    private PredicateInstance hasMonths;
    private PredicateInstance hasFeatures;
    protected RegionMigration(RDF4JTemplate rdf4jMethods) {
        super(rdf4jMethods, "Region");
        initHasCost();
        initHasFeatures();
        initHasMonths();
    }

    private void initHasCost() {
        hasCost = new PredicateInstance(
                rdf4jMethods, "hasCost",
                (instance) -> instance
                        .builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.LABEL, "connect to cost")
        );
    }


    private void initHasMonths() {
        hasMonths = new PredicateInstance(
                rdf4jMethods, "hasMonths",
                (instance) -> instance
                        .builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.LABEL, "connect to region months")
        );
    }

    private void initHasFeatures() {
        hasFeatures = new PredicateInstance(
                rdf4jMethods, "hasFeatures",
                (instance) -> instance
                        .builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.LABEL, "connect to region features")
        );
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBPROPERTYOF, DBPEDIA.REGION)
                .add(get(), RDFS.RANGE, RDFS.RESOURCE);
    }

    @Override
    public void setup() {
        super.setup();
        hasCost.setup();
        hasMonths.setup();
        hasFeatures.setup();
    }

    @Override
    public void migrate() {
        super.migrate();
        hasCost.migrate();
        hasMonths.migrate();
        hasFeatures.migrate();
    }
}
