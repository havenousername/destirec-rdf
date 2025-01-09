package org.destirec.destirec.rdf4j.preferences.months;

import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.interfaces.ModelFields;
import org.eclipse.rdf4j.model.IRI;

import java.time.Month;
import java.util.Map;

public record MonthDto(IRI id, Month month, float monthRange) implements Dto {
    @Override
    public Map<ModelFields.Field, String> getMap() {
        return Map.of(
                MonthModel.Fields.MONTH, "--" + month.getValue(),
                MonthModel.Fields.RANGE,  String.valueOf(monthRange)
        );
    }
}
