package org.destirec.destirec.rdf4j.region;

import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.destirec.destirec.rdf4j.months.MonthDao;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.region.cost.CostDao;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDao;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.utils.SimpleDtoTransformations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RegionDtoCreator implements DtoCreator<RegionDto, RegionConfig.Fields> {
    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();
    private final FeatureDao featureDao;
    private final MonthDao monthDao;
    private final CostDao costDao;

    public RegionDtoCreator(FeatureDao featureDao, MonthDao monthDao, CostDao costDao) {
        this.featureDao = featureDao;
        this.monthDao = monthDao;
        this.costDao = costDao;
    }

    @Override
    public RegionDto create(IRI id, Map<RegionConfig.Fields, String> map) {
        List<String> monthsString = SimpleDtoTransformations.toListString(map.get(RegionConfig.Fields.MONTHS));
        List<MonthDto> months = monthsString
                .stream()
                .map(month -> monthDao.getById(monthDao.getValueFactory().createIRI(month)))
                .toList();

        List<String> featuresString = SimpleDtoTransformations.toListString(map.get(RegionConfig.Fields.FEATURES));
        List<FeatureDto> features = featuresString
                .stream()
                .map(feature -> featureDao.getById(valueFactory.createIRI(feature)))
                .toList();
        CostDto cost = costDao.getById(valueFactory.createIRI(map.get(RegionConfig.Fields.COST)));
        return new RegionDto(
                id,
                cost,
                months,
                features
        );
    }

    @Override
    public RegionDto create(Map<RegionConfig.Fields, String> map) {
        return create(null, map);
    }
}
