package org.destirec.destirec.rdf4j.interfaces;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfResource;

import java.util.List;

public interface Predicate {
    Resource get();
    void setup();

    void migrate();

    void addRuleset();

    void setNamespaces(List<Namespace> namespaces);

    void setGraphName(String name);

    void setupAndMigrate();

    RdfResource getResource();
}
