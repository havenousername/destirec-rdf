package org.destirec.destirec.rdf4j.region.apiDto;

import lombok.Getter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.poi.POIDto;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.eclipse.rdf4j.model.IRI;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@ToString
public class RegionDtoWithChildren extends RegionDto {
    private final List<RegionDto> children;
    private final List<POIDto> pois;

    public RegionDtoWithChildren(IRI id, String name, RegionNames.Individuals.RegionTypes type, @Nullable IRI parentRegion, @Nullable IRI sourceIRI, @Nullable CostDto cost, List<MonthDto> months, List<FeatureDto> features, @Nullable String iso, @Nullable IRI geoShape, List<RegionDto> children, List<POIDto> pois) {
        super(id, name, type, parentRegion, sourceIRI, cost, months, features, iso, geoShape);
        this.children = children;
        this.pois = pois;
    }

    public RegionDtoWithChildren(RegionDto dto, List<RegionDto> children, List<POIDto> pois, List<FeatureDto> features) {
        super(dto.getId(), dto.getName(), dto.getType(), dto.getParentRegion(), dto.getSourceIRI(), dto.getCost(), dto.getMonths(), features, dto.getIso(), dto.getGeoShape());
        this.children = children;
        this.pois = pois;
    }
}
