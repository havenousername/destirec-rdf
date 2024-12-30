package org.destirec.destirec.rdf4j.interfaces;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;

import java.util.List;

public interface Predicate {
    IRI get();
    void setup();

    void migrate();

    void setNamespaces(List<Namespace> namespaces);

    void setGraphName(String name);

    void setupAndMigrate();
}
