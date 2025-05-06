package org.destirec.destirec.rdf4j.region.feature;

import lombok.Getter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.attribute.AttributeDto;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;

@ToString
@Getter
public final class FeatureDto extends AttributeDto implements Dto {
    private final RegionFeatureNames.Individuals.RegionFeature regionFeature;

    public FeatureDto(IRI id, int hasScore, boolean isActive, RegionFeatureNames.Individuals.RegionFeature regionFeature) {
        super(id, hasScore, isActive);
        this.regionFeature = regionFeature;
    }

    @Override
    public Map<ConfigFields.Field, String> getMap() {
        return Map.of(
                FeatureConfig.Fields.HAS_SCORE, String.valueOf(hasScore),
                FeatureConfig.Fields.IS_ACTIVE, String.valueOf(isActive),
                FeatureConfig.Fields.HAS_REGION_FEATURE, String.valueOf(regionFeature)
        );
    }
}
