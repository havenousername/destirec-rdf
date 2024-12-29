package org.destirec.destirec.rdf4j.dao.interfaces;

import org.eclipse.rdf4j.model.IRI;

import java.util.Map;

public interface DtoCreator<Dto, Field> {
    Dto create(IRI id, Map<Field, String> map);
}
