package org.destirec.destirec.rdf4j.preferences;

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
public class PreferenceMigration extends Migration {
    protected PredicateInstance isPriceImportant;
    protected PredicateInstance priceRange;

    protected PredicateInstance isPopularityImportant;

    protected PredicateInstance popularityRange;

    protected PreferenceMigration(RDF4JTemplate rdf4jMethods) {
        super(rdf4jMethods, "Preference");

        initPrice();
        initPopularity();
    }

    private void initPrice() {
        isPriceImportant = new PredicateInstance(
                rdf4jMethods, "isPriceImportant",
                (instance) -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.LABEL, "consider price or not")
                        .add(instance.predicate(), RDFS.RANGE, XSD.BOOLEAN)
        );


        priceRange = new PredicateInstance(
                rdf4jMethods, "priceRange",
                instance -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.LABEL, "range of the price")
                        .add(instance.predicate(), RDFS.RANGE, XSD.FLOAT)
        );
    }


    private void initPopularity() {
        isPopularityImportant = new PredicateInstance(
                rdf4jMethods, "isPopularityImportant",
                (instance) -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.LABEL, "consider popularity or not")
                        .add(instance.predicate(), RDFS.RANGE, XSD.BOOLEAN)
        );


        popularityRange = new PredicateInstance(
                rdf4jMethods, "popularityRange",
                instance -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.LABEL, "range of the popularity")
                        .add(instance.predicate(), RDFS.RANGE, XSD.FLOAT)
        );
    }

    @Override
    public void setup() {
        super.setup();
        isPriceImportant.setup();
        priceRange.setup();
        isPopularityImportant.setup();
        popularityRange.setup();
    }

    @Override
    public void migrate() {
        super.migrate();
        isPriceImportant.migrate();
        priceRange.migrate();
        isPopularityImportant.migrate();
        popularityRange.migrate();
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(get(), RDFS.SUBPROPERTYOF, WIKIDATA.PREFERENCE)
                .add(get(), RDFS.RANGE, RDFS.RESOURCE);
    }
}
