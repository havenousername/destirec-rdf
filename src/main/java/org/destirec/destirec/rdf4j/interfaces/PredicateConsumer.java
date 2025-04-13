package org.destirec.destirec.rdf4j.interfaces;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.ModelBuilder;

public record PredicateConsumer(ModelBuilder builder, Resource predicate) {}
