package org.destirec.destirec.rdf4j.user.preferences;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.destirec.destirec.rdf4j.months.MonthDao;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.utils.SimpleDtoTransformations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.stereotype.Component;

import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class PreferenceDtoCreator implements DtoCreator<PreferenceDto, PreferenceConfig.Fields> {
    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();
    private final MonthDao monthDao;

    public PreferenceDtoCreator(MonthDao monthDao) {
        this.monthDao = monthDao;
    }

    @Override
    public PreferenceDto create(IRI id, Map<PreferenceConfig.Fields, String> map) {
        List<String> monthsString = SimpleDtoTransformations.toListString(map.get(PreferenceConfig.Fields.MONTHS));
        List<MonthDto> months = monthsString
                .stream()
                .map(month -> monthDao.getById(monthDao.getValueFactory().createIRI(month)))
                .toList();
        return new PreferenceDto(
                id,
                valueFactory.createIRI(map.get(PreferenceConfig.Fields.PREFERENCE_AUTHOR)),
                Boolean.getBoolean(map.get(PreferenceConfig.Fields.IS_PRICE_IMPORTANT)),
                Float.parseFloat(map.get(PreferenceConfig.Fields.PRICE_RANGE)),
                Boolean.getBoolean(map.get(PreferenceConfig.Fields.IS_POPULARITY_IMPORTANT)),
                Float.parseFloat(map.get(PreferenceConfig.Fields.POPULARITY_RANGE)),
                months
        );
    }


    public PreferenceDto create(IRI id, IRI userIri, Map<PreferenceConfig.Fields, String> map, Float[] months) {
        return new PreferenceDto(
                id,
                userIri,
                Boolean.getBoolean(map.get(PreferenceConfig.Fields.IS_PRICE_IMPORTANT)),
                Float.parseFloat(map.get(PreferenceConfig.Fields.PRICE_RANGE)),
                Boolean.getBoolean(map.get(PreferenceConfig.Fields.IS_POPULARITY_IMPORTANT)),
                Float.parseFloat(map.get(PreferenceConfig.Fields.POPULARITY_RANGE)),
                IntStream.rangeClosed(0, months.length - 1).mapToObj(i -> new MonthDto(null, Month.of(i + 1), months[i])).toList()
        );
    }

    @Override
    public PreferenceDto create(Map<PreferenceConfig.Fields, String> map) {
        return create(null, map);
    }
}
