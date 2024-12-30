package org.destirec.destirec.rdf4j.version;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VersionDtoCreator implements DtoCreator<VersionDto, VersionModel.Fields> {

    @Override
    public VersionDto create(IRI id, Map<VersionModel.Fields, String> map) {
        return new VersionDto(id, Float.parseFloat(map.get(VersionModel.Fields.VERSION)));
    }

    @Override
    public VersionDto create(Map<VersionModel.Fields, String> map) {
        return create(null, map);
    }
}
