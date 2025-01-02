package org.destirec.destirec.rdf4j.preferences.months;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.time.Month;
import java.util.Map;

@Component
public class MonthDtoCreator implements DtoCreator<MonthDto, MonthModel.Fields> {
    @Override
    public MonthDto create(IRI id, Map<MonthModel.Fields, String> map) {
        return new MonthDto(id, Month.of(Integer.parseInt(map.get(MonthModel.Fields.MONTH).replace("-", ""))), Float.parseFloat(map.get(MonthModel.Fields.RANGE)));
    }

    @Override
    public MonthDto create(Map<MonthModel.Fields, String> map) {
        return create(null, map);
    }
}
