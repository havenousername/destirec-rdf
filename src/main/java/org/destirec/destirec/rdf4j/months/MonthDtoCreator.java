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
        return new MonthDto(
                id,
                Integer.parseInt(map.get(MonthConfig.Fields.HAS_SCORE)),
                Boolean.parseBoolean(map.get(MonthConfig.Fields.IS_ACTIVE)),
                map.get(MonthConfig.Fields.MONTH_NAME),
                Integer.parseInt(map.get(MonthConfig.Fields.POSITION))
        );
    }

    @Override
    public MonthDto create(Map<MonthConfig.Fields, String> map) {
        return create(null, map);
    }

    public MonthDto create(Map.Entry<Month, Integer> month) {
        return new MonthDto(null, 100, true, month.getKey().name(), month.getValue());
    }

    public MonthDto create(Month month, int value) {
        return new MonthDto(null, 100, true, month.name(), value);
    }
}
