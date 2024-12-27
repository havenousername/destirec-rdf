package org.destirec.destirec.rdf4j.model.resource;

import org.destirec.destirec.rdf4j.model.predicates.Predicate;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;

import java.util.Map;

public interface ResourceRDF<T> extends Predicate {
    void createResource(ModelBuilder builder, String graphName);
    void addPropertiesToResource(ModelBuilder builder, String graphName, IRI resource, Map<T, String> values);
    String getResourceLocation();
}
