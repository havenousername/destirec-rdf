package org.destirec.destirec.rdf4j.preferences;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.interfaces.ModelFields;
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

    public Map<ModelFields.Field, String> getMap() {
        return Map.of(
          PreferenceModel.Fields.IS_PRICE_IMPORTANT, Boolean.toString(isPriceImportant),
          PreferenceModel.Fields.PRICE_RANGE, Float.toString(priceRange),
          PreferenceModel.Fields.IS_POPULARITY_IMPORTANT, Boolean.toString(isPopularityImportant),
          PreferenceModel.Fields.POPULARITY_RANGE, Float.toString(popularityRange),
          PreferenceModel.Fields.PREFERENCE_AUTHOR, preferenceAuthor.stringValue(),
          PreferenceModel.Fields.MONTHS, monthsDto.stream()
                        .map(month -> month.id() + ",")
                        .collect(Collectors.joining())
        );
    }

    @Override
    public IRI id() {
        return id;
    }
}
