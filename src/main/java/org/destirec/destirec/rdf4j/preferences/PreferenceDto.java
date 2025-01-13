package org.destirec.destirec.rdf4j.preferences;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.destirec.destirec.rdf4j.preferences.months.MonthDto;
import org.eclipse.rdf4j.model.IRI;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ToString
@Getter
@AllArgsConstructor
public class PreferenceDto implements Dto {
    private final IRI id;
    private final IRI preferenceAuthor;
    private final boolean  isPriceImportant;
    private final float priceRange;
    private final boolean isPopularityImportant;
    private float popularityRange;
    @Setter
    private List<MonthDto> monthsDto;

    public Map<ConfigFields.Field, String> getMap() {
        return Map.of(
          PreferenceConfig.Fields.IS_PRICE_IMPORTANT, Boolean.toString(isPriceImportant),
          PreferenceConfig.Fields.PRICE_RANGE, Float.toString(priceRange),
          PreferenceConfig.Fields.IS_POPULARITY_IMPORTANT, Boolean.toString(isPopularityImportant),
          PreferenceConfig.Fields.POPULARITY_RANGE, Float.toString(popularityRange),
          PreferenceConfig.Fields.PREFERENCE_AUTHOR, preferenceAuthor.stringValue(),
          PreferenceConfig.Fields.MONTHS, monthsDto.stream()
                        .map(month -> month.id() + ",")
                        .collect(Collectors.joining())
        );
    }

    @Override
    public IRI id() {
        return id;
    }
}
