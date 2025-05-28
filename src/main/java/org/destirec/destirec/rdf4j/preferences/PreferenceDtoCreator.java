package org.destirec.destirec.rdf4j.preferences;

import org.destirec.destirec.rdf4j.attribute.AttributeDto;
import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.destirec.destirec.rdf4j.months.MonthDao;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.region.cost.CostDao;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDao;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.utils.SimpleDtoTransformations;
import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.stereotype.Component;

import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class PreferenceDtoCreator implements DtoCreator<PreferenceDto, PreferenceConfig.Fields> {
    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();
    private final MonthDao monthDao;

    private final FeatureDao featureDao;

    private final CostDao costDao;

    private final PreferenceConfig config;

    public PreferenceDtoCreator(MonthDao monthDao, FeatureDao featureDao, CostDao costDao, PreferenceConfig config) {
        this.monthDao = monthDao;
        this.featureDao = featureDao;
        this.costDao = costDao;
        this.config = config;
    }

    @Override
    public PreferenceDto create(IRI id, Map<PreferenceConfig.Fields, String> map) {
        List<String> monthsString = SimpleDtoTransformations.toListString(map.get(PreferenceConfig.Fields.HAS_MONTH));
        List<MonthDto> months = monthsString
                .stream()
                .map(month -> monthDao.getById(monthDao.getValueFactory().createIRI(month)))
                .toList();

        List<String> featuresList = SimpleDtoTransformations.toListString(map.get(PreferenceConfig.Fields.HAS_FEATURE));
        List<FeatureDto> featureDtos = featuresList
                .stream()
                .map(pref -> featureDao.getById(featureDao.getValueFactory().createIRI(pref)))
                .toList();

        CostDto costDto = costDao.getById(valueFactory.createIRI(map.get(PreferenceConfig.Fields.HAS_COST)));
        return new PreferenceDto(
                id,
                valueFactory.createIRI(map.get(PreferenceConfig.Fields.PREFERENCE_AUTHOR)),
                featureDtos,
                costDto,
                months
        );
    }


    public PreferenceDto create(
            IRI id,
            IRI userIri,
            Map<PreferenceConfig.Fields, String> map,
            Map<RegionFeatureNames.Individuals.RegionFeature, AttributeDto> features,
            List<AttributeDto> months,
            CostDto cost
    ) {
        return new PreferenceDto(
                id,
                userIri,
                features.entrySet().stream().map((f) ->
                        new FeatureDto(null, f.getValue().getHasScore(), f.getValue().isActive(), f.getKey())).toList(),
                cost,
                IntStream.range(0, months.size()).mapToObj(i ->
                        new MonthDto(null, months.get(i).getHasScore(), months.get(i).isActive(), Month.of(i + 1).name(), i)).toList()
        );
    }

    public IRI createId(String id) {
        return valueFactory.createIRI(config.getResourceLocation() + id);
    }

    public PreferenceDto create(
            IRI userId,
            List<FeatureDto> features,
            List<MonthDto> months,
            CostDto cost
    ) {
        return new PreferenceDto(
                null,
                userId,
                features,
                cost,
                months
        );
    }

    @Override
    public PreferenceDto create(Map<PreferenceConfig.Fields, String> map) {
        return create(null, map);
    }
}
