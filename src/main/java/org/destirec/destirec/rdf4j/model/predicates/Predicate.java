package org.destirec.destirec.rdf4j.model.predicates;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;

public interface Predicate {
    IRI get();
    void setup(ModelBuilder builder, String graphName);
}
