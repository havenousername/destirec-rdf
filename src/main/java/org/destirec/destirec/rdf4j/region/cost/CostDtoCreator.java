package org.destirec.destirec.rdf4j.region.cost;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CostDtoCreator implements DtoCreator<CostDto, CostConfig.Fields> {
    @Override
    public CostDto create(IRI id, Map<CostConfig.Fields, String> map) {
        return new CostDto(
                id,
                Integer.parseInt(map.get(CostConfig.Fields.HAS_SCORE)),
                Boolean.parseBoolean(map.get(CostConfig.Fields.IS_ACTIVE)),
                Integer.parseInt(map.get(CostConfig.Fields.COST_PER_WEEK)),
                Integer.parseInt(map.get(CostConfig.Fields.BUDGET_LEVEL))
        );
    }

    @Override
    public CostDto create(Map<CostConfig.Fields, String> map) {
        return create(null, map);
    }


    public CostDto create(int costPerWeek, int budgetLevel) {
        return new CostDto(null, budgetLevel, true, costPerWeek, budgetLevel);
    }

    public CostDto create(int costPerWeek, int budgetLevel, boolean isActive) {
        return new CostDto(null, budgetLevel, isActive, costPerWeek, budgetLevel);
    }
}
