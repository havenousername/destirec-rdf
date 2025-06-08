package org.destirec.destirec.rdf4j.interfaces;


import java.util.List;

public interface ContainerVisitor<T> {
    void visit(T visitor);
    void visit(List<T> visitor);
}

