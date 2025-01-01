package org.destirec.destirec.rdf4j.interfaces;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;

public record PredicateConsumer(ModelBuilder builder, IRI predicate) {}
