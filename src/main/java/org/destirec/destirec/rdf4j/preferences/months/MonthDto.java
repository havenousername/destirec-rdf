package org.destirec.destirec.rdf4j.preferences.months;

import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
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
}
