package org.destirec.destirec.rdf4j.region.feature;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FeatureDtoCreator implements DtoCreator<FeatureDto, FeatureConfig.Fields> {
    @Override
    public FeatureDto create(IRI id, Map<FeatureConfig.Fields, String> map) {
        return new FeatureDto(
                id,
                map.get(FeatureConfig.Fields.KIND),
                Float.parseFloat(map.get(FeatureConfig.Fields.VALUE))
        );
    }

    @Override
    public FeatureDto create(Map<FeatureConfig.Fields, String> map) {
        return create(null, map);
    }

    public FeatureDto create(Map.Entry<String, Integer> entry) {
        return new FeatureDto(null, entry.getKey(), entry.getValue());
    }
}
