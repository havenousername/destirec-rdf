package org.destirec.destirec.rdf4j.attribute;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;

@ToString
@Getter
@AllArgsConstructor
public class AttributeDto implements Dto {
    protected final IRI id;
    protected final int hasScore;
    protected final boolean isActive;

    @Override
    public Map<AttributeConfig.Field, String> getMap() {
        return Map.of(
                AttributeConfig.Fields.HAS_SCORE, String.valueOf(hasScore),
                AttributeConfig.Fields.IS_ACTIVE, String.valueOf(isActive)
        );
    }

    @Override
    public IRI id() {
        return id;
    }
}
