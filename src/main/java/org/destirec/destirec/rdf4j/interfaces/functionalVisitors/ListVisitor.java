package org.destirec.destirec.rdf4j.interfaces.functionalVisitors;

import java.util.List;

@FunctionalInterface
public interface ListVisitor<T> {
    void visit(List<T> items);
}
