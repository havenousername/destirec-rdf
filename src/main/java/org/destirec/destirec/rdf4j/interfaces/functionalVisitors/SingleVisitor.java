package org.destirec.destirec.rdf4j.interfaces.functionalVisitors;

@FunctionalInterface
public interface SingleVisitor<T> {
    void visit(T item);
}
