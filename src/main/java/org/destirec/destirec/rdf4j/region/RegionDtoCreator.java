package org.destirec.destirec.rdf4j.region;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.destirec.destirec.rdf4j.months.MonthDao;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.region.apiDto.ExternalRegionDto;
import org.destirec.destirec.rdf4j.region.apiDto.SimpleRegionDto;
import org.destirec.destirec.rdf4j.region.cost.CostDao;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDao;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.utils.SimpleDtoTransformations;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RegionDtoCreator implements DtoCreator<RegionDto, RegionConfig.Fields> {
    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();
    private final FeatureDao featureDao;
    private final MonthDao monthDao;
    private final CostDao costDao;

    private final RegionConfig regionConfig;

    public RegionDtoCreator(FeatureDao featureDao, MonthDao monthDao, CostDao costDao, RegionConfig regionConfig) {
        this.featureDao = featureDao;
        this.monthDao = monthDao;
        this.costDao = costDao;
        this.regionConfig = regionConfig;
    }

    @Override
    public RegionDto create(IRI id, Map<RegionConfig.Fields, String> map) {
        List<String> monthsString = Optional.ofNullable(map.get(RegionConfig.Fields.MONTHS))
                .map(SimpleDtoTransformations::toListString)
                .orElse(Collections.emptyList());
        List<MonthDto> months = monthsString
                .stream()
                .map(month -> monthDao.getById(monthDao.getValueFactory().createIRI(month)))
                .toList();

        List<String> featuresString = Optional.ofNullable(map.get(RegionConfig.Fields.FEATURES))
                .map(SimpleDtoTransformations::toListString)
                .orElse(Collections.emptyList());
        List<FeatureDto> features = featuresString
                .stream()
                .map(feature -> featureDao.getById(valueFactory.createIRI(feature)))
                .toList();
        CostDto cost = Optional.ofNullable(map.get(RegionConfig.Fields.COST))
                .map(valueFactory::createIRI)
                .map(costDao::getById)
                .orElse(null);
        IRI parent = Optional.ofNullable(map.get(RegionConfig.Fields.PARENT_REGION))
                .map(valueFactory::createIRI)
                .orElse(null);

        IRI source = Optional.ofNullable(map.get(RegionConfig.Fields.SOURCE))
                .map(valueFactory::createIRI)
                .orElse(null);

        String regionType = map.get(RegionConfig.Fields.REGION_TYPE);
        RegionNames.Individuals.RegionTypes type = regionType != null ?
                RegionNames.Individuals.RegionTypes.valueOf(regionType) : null;
        return new RegionDto(
                id,
                map.get(RegionConfig.Fields.NAME),
                type,
                parent,
                source,
                cost,
                months,
                features
        );
    }

    public IRI createId(String id) {
        if (id == null) {
            return null;
        }
        return valueFactory.createIRI(regionConfig.getResourceLocation() + id);
    }

    public RegionDto create(
            ExternalRegionDto regionDto,
            List<FeatureDto> features,
            List<MonthDto> months,
            CostDto cost
    ) {
        return new RegionDto(
                createId(regionDto.id()),
                regionDto.getName(),
                null,
                createId(regionDto.parentRegion()),
                regionDto.sourceIRI() == null  ? null :valueFactory.createIRI(regionDto.sourceIRI()),
                cost,
                months,
                features
        );
    }

    public RegionDto create(
            SimpleRegionDto dto,
            IRI parentRegion
    ) {
        IRI id = createId(dto.getId());
        return new RegionDto(
                id,
                dto.getName(),
                dto.getType(),
                parentRegion,
                dto.getSource(),
                null,
                null,
                null
        );
    }

    @Override
    public RegionDto create(Map<RegionConfig.Fields, String> map) {
        return create(null, map);
    }
}
