package org.destirec.destirec.rdf4j.version;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VersionDtoCreator implements DtoCreator<VersionDto, VersionConfig.Fields> {

    @Override
    public VersionDto create(IRI id, Map<VersionConfig.Fields, String> map) {
        return new VersionDto(id, Float.parseFloat(map.get(VersionConfig.Fields.VERSION)));
    }

    @Override
    public VersionDto create(Map<VersionConfig.Fields, String> map) {
        return create(null, map);
    }
}
