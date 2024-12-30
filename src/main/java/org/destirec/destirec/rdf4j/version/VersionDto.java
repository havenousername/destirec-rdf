package org.destirec.destirec.rdf4j.version;

import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.interfaces.ModelFields;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;

public record VersionDto(IRI id, float version) implements Dto {
    @Override
    public Map<ModelFields.Field, String> getMap() {
        return Map.of(VersionModel.Fields.VERSION, String.valueOf(version));
    }
}
