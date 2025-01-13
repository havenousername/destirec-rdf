package org.destirec.destirec.rdf4j.interfaces;

import org.eclipse.rdf4j.model.IRI;

import java.util.Map;

public interface Dto {
    Map<ConfigFields.Field, String> getMap();
    IRI id();
}
