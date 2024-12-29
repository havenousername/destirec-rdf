package org.destirec.destirec.rdf4j.dao.interfaces;

import org.eclipse.rdf4j.model.IRI;

import java.util.List;

public interface Dto {
    List<String> getList();
    IRI id();
}
