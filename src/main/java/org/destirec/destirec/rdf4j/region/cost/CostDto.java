package org.destirec.destirec.rdf4j.region.cost;

import lombok.Getter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.attribute.AttributeDto;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;

@ToString
@Getter
public final class CostDto extends AttributeDto implements Dto {
    private final int costPerWeek;
    private final int budgetLevel;

    public CostDto(IRI id, int hasScore, boolean isActive, int costPerWeek, int budgetLevel) {
        super(id, hasScore, isActive);
        this.costPerWeek = costPerWeek;
        this.budgetLevel = budgetLevel;
    }

    @Override
    public Map<ConfigFields.Field, String> getMap() {
        return Map.of(
                CostConfig.Fields.COST_PER_WEEK, String.valueOf(costPerWeek),
                CostConfig.Fields.BUDGET_LEVEL, String.valueOf(budgetLevel),
                CostConfig.Fields.HAS_SCORE, String.valueOf(hasScore),
                CostConfig.Fields.IS_ACTIVE, String.valueOf(isActive)
        );
    }
}
