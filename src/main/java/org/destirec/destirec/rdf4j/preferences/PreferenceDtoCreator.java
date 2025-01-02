package org.destirec.destirec.rdf4j.preferences;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.destirec.destirec.rdf4j.preferences.months.MonthDto;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.stereotype.Component;

import java.time.Month;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class PreferenceDtoCreator implements DtoCreator<PreferenceDto, PreferenceModel.Fields> {
    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();
    @Override
    public PreferenceDto create(IRI id, Map<PreferenceModel.Fields, String> map) {
        return new PreferenceDto(
                id,
                valueFactory.createIRI(map.get(PreferenceModel.Fields.PREFERENCE_AUTHOR)),
                Boolean.getBoolean(map.get(PreferenceModel.Fields.IS_PRICE_IMPORTANT)),
                Float.parseFloat(map.get(PreferenceModel.Fields.PRICE_RANGE)),
                Boolean.getBoolean(map.get(PreferenceModel.Fields.IS_POPULARITY_IMPORTANT)),
                Float.parseFloat(map.get(PreferenceModel.Fields.POPULARITY_RANGE)),
                new ArrayList<>()
        );
    }


    public PreferenceDto create(IRI id, IRI userIri, Map<PreferenceModel.Fields, String> map, Float[] months) {
        return new PreferenceDto(
                id,
                userIri,
                Boolean.getBoolean(map.get(PreferenceModel.Fields.IS_PRICE_IMPORTANT)),
                Float.parseFloat(map.get(PreferenceModel.Fields.PRICE_RANGE)),
                Boolean.getBoolean(map.get(PreferenceModel.Fields.IS_POPULARITY_IMPORTANT)),
                Float.parseFloat(map.get(PreferenceModel.Fields.POPULARITY_RANGE)),
                IntStream.rangeClosed(0, months.length - 1).mapToObj(i -> new MonthDto(null, Month.of(i + 1), months[i])).toList()
        );
    }

    @Override
    public PreferenceDto create(Map<PreferenceModel.Fields, String> map) {
        return create(null, map);
    }
}
