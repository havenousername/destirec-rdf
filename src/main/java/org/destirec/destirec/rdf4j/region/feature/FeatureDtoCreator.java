package org.destirec.destirec.rdf4j.region.feature;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames;
import org.eclipse.rdf4j.model.IRI;
import org.javatuples.Pair;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FeatureDtoCreator implements DtoCreator<FeatureDto, FeatureConfig.Fields> {
    @Override
    public FeatureDto create(IRI id, Map<FeatureConfig.Fields, String> map) {
        return new FeatureDto(
                id,
                Integer.parseInt(map.get(FeatureConfig.Fields.HAS_SCORE)),
                Boolean.parseBoolean(map.get(FeatureConfig.Fields.IS_ACTIVE)),
                RegionFeatureNames.Individuals.RegionFeature.fromIri(map.get(FeatureConfig.Fields.HAS_REGION_FEATURE))
        );
    }

    @Override
    public FeatureDto create(Map<FeatureConfig.Fields, String> map) {
        return create(null, map);
    }

    public FeatureDto createFromEnum(Map.Entry<RegionFeatureNames.Individuals.RegionFeature, Integer> entry) {
        return new FeatureDto(
                null,
                entry.getValue(),
                true,
                entry.getKey()
        );
    }

    public FeatureDto createFromTuple(RegionFeatureNames.Individuals.RegionFeature feature, Pair<Integer, Boolean> entry) {
        return new FeatureDto(
                null,
                entry.getValue0(),
                entry.getValue1(),
                feature
        );
    }

    public FeatureDto create(Map.Entry<String, Integer> entry) {
        return new FeatureDto(
                null,
                entry.getValue(),
                true,
                RegionFeatureNames.Individuals.RegionFeature.fromIri(entry.getKey())
        );
    }
}
