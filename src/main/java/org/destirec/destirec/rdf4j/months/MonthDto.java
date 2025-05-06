package org.destirec.destirec.rdf4j.months;

import org.destirec.destirec.rdf4j.attribute.AttributeDto;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.eclipse.rdf4j.model.IRI;

import java.time.Month;
import java.util.Map;

public class MonthDto extends AttributeDto implements Dto {
    private final String name;
    private final int position;

    public MonthDto(IRI id, int hasScore, boolean isActive, String name, int position) {
        super(id, hasScore, isActive);
        this.name = name;
        this.position = position;
    }

    public Month getMonth() {
        return Month.of(position);
    }

    @Override
    public Map<ConfigFields.Field, String> getMap() {
        return Map.of(
                MonthConfig.Fields.MONTH_NAME, String.valueOf(name),
                MonthConfig.Fields.IS_ACTIVE, String.valueOf(isActive),
                MonthConfig.Fields.HAS_SCORE, String.valueOf(hasScore),
                MonthConfig.Fields.POSITION, String.valueOf(position)
        );
    }
}
