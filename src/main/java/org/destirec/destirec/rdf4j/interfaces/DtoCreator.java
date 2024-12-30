package org.destirec.destirec.rdf4j.interfaces;

import org.eclipse.rdf4j.model.IRI;

import java.util.Map;

public interface DtoCreator<Dto, Field> {
    Dto create(IRI id, Map<Field, String> map);
    Dto create(Map<Field, String> map);
}
