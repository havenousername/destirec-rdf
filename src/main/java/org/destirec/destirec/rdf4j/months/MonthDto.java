package org.destirec.destirec.rdf4j.months;

import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.eclipse.rdf4j.model.IRI;

import java.time.Month;
import java.util.Map;

public record MonthDto(IRI id, Month month, float monthRange) implements Dto {
    @Override
    public Map<ConfigFields.Field, String> getMap() {
        return Map.of(
                MonthConfig.Fields.MONTH, "--" + month.getValue(),
                MonthConfig.Fields.RANGE,  String.valueOf(monthRange)
        );
    }

    public boolean isSame(Month month, Integer value) {
        return month.equals(this.month) && value.floatValue() == monthRange;
    }
}
