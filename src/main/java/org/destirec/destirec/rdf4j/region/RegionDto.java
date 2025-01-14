package org.destirec.destirec.rdf4j.region;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.utils.SimpleDtoTransformations;
import org.eclipse.rdf4j.model.IRI;

import java.util.List;
import java.util.Map;

@ToString
@Getter
@AllArgsConstructor
public class RegionDto implements Dto {
    public final IRI id;
    private final String name;
    private final IRI parentRegion;
    private CostDto cost;
    private List<MonthDto> months;
    private List<FeatureDto> features;

    @Override
    public Map<ConfigFields.Field, String> getMap() {
        return Map.of(
                RegionConfig.Fields.FEATURES, SimpleDtoTransformations.toStringIds(features),
                RegionConfig.Fields.MONTHS, SimpleDtoTransformations.toStringIds(months),
                RegionConfig.Fields.COST, cost.id().stringValue(),
                RegionConfig.Fields.PARENT_REGION, parentRegion.stringValue(),
                RegionConfig.Fields.NAME, name
        );
    }

    @Override
    public IRI id() {
        return id;
    }
}
