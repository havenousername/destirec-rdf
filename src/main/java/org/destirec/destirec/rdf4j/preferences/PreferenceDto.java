package org.destirec.destirec.rdf4j.preferences;

import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.interfaces.ModelFields;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;

public record PreferenceDto(
        IRI id,

        IRI preferenceAuthor,
        boolean isPriceImportant,

        float priceRange,

        boolean isPopularityImportant,

        float popularityRange

) implements Dto {
    public Map<ModelFields.Field, String> getMap() {
        return Map.of(
          PreferenceModel.Fields.IS_PRICE_IMPORTANT, Boolean.toString(isPriceImportant),
          PreferenceModel.Fields.PRICE_RANGE, Float.toString(priceRange),
          PreferenceModel.Fields.IS_POPULARITY_IMPORTANT, Boolean.toString(isPopularityImportant),
          PreferenceModel.Fields.POPULARITY_RANGE, Float.toString(popularityRange),
          PreferenceModel.Fields.PREFERENCE_AUTHOR, preferenceAuthor.stringValue());
    }
}
