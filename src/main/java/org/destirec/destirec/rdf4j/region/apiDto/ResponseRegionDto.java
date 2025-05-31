package org.destirec.destirec.rdf4j.region.apiDto;

import lombok.Getter;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.eclipse.rdf4j.model.IRI;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResponseRegionDto extends RegionDto {
    public ResponseRegionDto(IRI id, String name, RegionNames.Individuals.RegionTypes type, @Nullable IRI parentRegion, @Nullable IRI sourceIRI, @Nullable CostDto cost, List<MonthDto> months, List<FeatureDto> features, @Nullable String iso, @Nullable IRI geoShape) {
        super(id, name, type, parentRegion, sourceIRI, cost, months, features, iso, geoShape);
        mapUrl = null;
    }

    public ResponseRegionDto(RegionDto dto, String mapUrl) {
        super(
                dto.id(),
                dto.getName(),
                dto.getType(),
                dto.getParentRegion(),
                dto.getSourceIRI(),
                dto.getCost(),
                dto.getMonths(),
                dto.getFeatures(),
                dto.getIso(),
                dto.getGeoShape());
        this.mapUrl = mapUrl;
    }

    @Getter
    private final String mapUrl;
}
