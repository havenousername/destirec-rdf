package org.destirec.destirec.rdf4j.preferences;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.utils.SimpleDtoTransformations;
import org.eclipse.rdf4j.model.IRI;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@Getter
@AllArgsConstructor
public final class PreferenceDto implements Dto {
    private final IRI id;
    private final IRI preferenceAuthor;
    @Setter
    private List<FeatureDto> featureDtos;
    @Setter
    @Nullable
    private CostDto costDto;

    @Setter
    @Nullable
    private List<MonthDto> monthDtos;

    public Map<ConfigFields.Field, String> getMap() {
        Map<ConfigFields.Field, String> map = new HashMap<>(Map.of(
                PreferenceConfig.Fields.HAS_FEATURE, SimpleDtoTransformations.toStringIds(featureDtos),
                PreferenceConfig.Fields.PREFERENCE_AUTHOR, preferenceAuthor.stringValue()
        ));

        if (monthDtos != null && !monthDtos.isEmpty() ) {
          map.put(PreferenceConfig.Fields.HAS_MONTH, SimpleDtoTransformations.toStringIds(monthDtos));
        }

        if (costDto != null) {
            map.put(PreferenceConfig.Fields.HAS_COST, costDto.getId().stringValue());
        }

        return map;
    }

    @Override
    public IRI id() {
        return id;
    }
}
