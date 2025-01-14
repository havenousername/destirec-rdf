package org.destirec.destirec.rdf4j.months;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.time.Month;
import java.util.Map;

@Component
public class MonthDtoCreator implements DtoCreator<MonthDto, MonthConfig.Fields> {
    @Override
    public MonthDto create(IRI id, Map<MonthConfig.Fields, String> map) {
        return new MonthDto(id, Month.of(Integer.parseInt(map.get(MonthConfig.Fields.MONTH).replace("-", ""))), Float.parseFloat(map.get(MonthConfig.Fields.RANGE)));
    }

    @Override
    public MonthDto create(Map<MonthConfig.Fields, String> map) {
        return create(null, map);
    }
}
