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
                Float.parseFloat(map.get(CostConfig.Fields.COST_PER_WEEK)),
                Float.parseFloat(map.get(CostConfig.Fields.BUDGET_LEVEL))
        );
    }

    @Override
    public CostDto create(Map<CostConfig.Fields, String> map) {
        return create(null, map);
    }
}
