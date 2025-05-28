package org.destirec.destirec.rdf4j.interfaces;

import org.eclipse.rdf4j.spring.support.RDF4JTemplate;

import java.util.function.Consumer;

public class IriMigrationInstance extends IriMigration {
    private final Consumer<PredicateConsumer> fx;
    public IriMigrationInstance(RDF4JTemplate rdf4jMethods, String iriName, Consumer<PredicateConsumer> setupPredicates) {
        super(rdf4jMethods, iriName);
        fx = setupPredicates;
    }

    @Override
    protected void setupProperties() {
        fx.accept(new PredicateConsumer(builder, get()));
    }
}
