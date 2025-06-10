package org.destirec.destirec.rdf4j.region.apiDto;

import lombok.Getter;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.eclipse.rdf4j.model.IRI;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class RegionDtoWithIds extends RegionDto {
    private final List<Pair<IRI, IRI>> children;
    private final List<IRI> featuresIds;

    public RegionDtoWithIds(IRI id, String name, RegionNames.Individuals.RegionTypes type, @Nullable IRI parentRegion, @Nullable IRI sourceIRI, @Nullable CostDto cost, List<MonthDto> months, List<FeatureDto> features, @Nullable String iso, String osmId, @Nullable IRI geoShape, List<Pair<IRI, IRI>> children, List<IRI> featuresIds) {
        super(id, name, type, parentRegion, sourceIRI, cost, months, features, iso, osmId, geoShape);
        this.children = children;
        this.featuresIds = featuresIds;
    }

    public RegionDtoWithIds(RegionDto dto, List<Pair<IRI, IRI>> children,  List<IRI> features) {
        super(dto.getId(), dto.getName(), dto.getType(), dto.getParentRegion(), dto.getSourceIRI(), dto.getCost(), dto.getMonths(), dto.getFeatures(), dto.getIso(), dto.getOsmId(), dto.getGeoShape());
        this.children = children;
        this.featuresIds = features;
    }
}