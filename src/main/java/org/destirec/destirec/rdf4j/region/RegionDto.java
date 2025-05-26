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
import org.destirec.destirec.utils.rdfDictionary.RegionNames.Individuals.RegionTypes;
import org.eclipse.rdf4j.model.IRI;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@Getter
@AllArgsConstructor
public class RegionDto implements Dto {
    public final IRI id;
    private final String name;
    private final RegionTypes type;

    @Nullable
    private final IRI parentRegion;

    @Nullable
    private final IRI sourceIRI;

    private CostDto cost;
    private List<MonthDto> months;
    private List<FeatureDto> features;

    @Override
    public Map<ConfigFields.Field, String> getMap() {
        Map<ConfigFields.Field, String> requiredFields = new HashMap<>(
                Map.of(
                        RegionConfig.Fields.NAME, name
                )
        );

        if (cost != null) {
            requiredFields.put(RegionConfig.Fields.COST, cost.id().stringValue());
        }

        if (features != null) {
            requiredFields.put(RegionConfig.Fields.FEATURES, SimpleDtoTransformations.toStringIds(features));
        }

        if (months != null) {
            requiredFields.put(RegionConfig.Fields.MONTHS, SimpleDtoTransformations.toStringIds(months));
        }

        if (sourceIRI != null) {
            requiredFields.put(RegionConfig.Fields.SOURCE, sourceIRI.stringValue());
        }
        if (parentRegion != null) {
            requiredFields.put(RegionConfig.Fields.PARENT_REGION, parentRegion.stringValue());
        }

        if (type != null) {
            requiredFields.put(RegionConfig.Fields.REGION_TYPE, type.iri().rdfIri().stringValue());
        }
        return requiredFields;
    }

    @Override
    public IRI id() {
        return id;
    }
}
