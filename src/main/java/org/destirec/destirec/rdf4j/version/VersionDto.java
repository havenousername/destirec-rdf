package org.destirec.destirec.rdf4j.version;

import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;

public record VersionDto(IRI id, float version) implements Dto {
    @Override
    public Map<ConfigFields.Field, String> getMap() {
        return Map.of(VersionConfig.Fields.VERSION, String.valueOf(version));
    }
}
