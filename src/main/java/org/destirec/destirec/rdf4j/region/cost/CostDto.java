package org.destirec.destirec.rdf4j.region.cost;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.eclipse.rdf4j.model.IRI;

import java.util.Map;

@ToString
@Getter
@AllArgsConstructor
public final class CostDto implements Dto {
    private final IRI id;
    private final float costPerWeek;
    private final float budgetLevel;
    @Override
    public Map<ConfigFields.Field, String> getMap() {
        return Map.of(
                CostConfig.Fields.COST_PER_WEEK, String.valueOf(costPerWeek),
                CostConfig.Fields.BUDGET_LEVEL, String.valueOf(budgetLevel)
        );
    }

    @Override
    public IRI id() {
        return id;
    }
}
