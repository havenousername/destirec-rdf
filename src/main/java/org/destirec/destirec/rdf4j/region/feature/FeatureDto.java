package org.destirec.destirec.rdf4j.region.feature;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;

@ToString
@Getter
@AllArgsConstructor
public final class FeatureDto implements Dto {
    private final IRI id;
    private final String kind;
    private final float value;

    @Override
    public Map<ConfigFields.Field, String> getMap() {
        return Map.of(
                FeatureConfig.Fields.KIND, kind,
                FeatureConfig.Fields.VALUE, String.valueOf(value)
        );
    }

    @Override
    public IRI id() {
        return id;
    }
}
