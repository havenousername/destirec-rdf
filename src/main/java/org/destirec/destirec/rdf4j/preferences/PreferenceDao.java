package org.destirec.destirec.rdf4j.preferences;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.months.MonthDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.region.cost.CostDao;
import org.destirec.destirec.rdf4j.region.feature.FeatureDao;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

@Getter
@Repository
public class PreferenceDao extends GenericDao<PreferenceConfig.Fields, PreferenceDto> {
    private final CostDao costDao;
    private final FeatureDao featureDao;
    private final MonthDao monthDao;
    public PreferenceDao(
            RDF4JTemplate rdf4JTemplate,
            PreferenceConfig modelFields,
            PreferenceMigration migration,
            PreferenceDtoCreator dtoCreator,
            DestiRecOntology ontology, CostDao costDao, FeatureDao featureDao, MonthDao monthDao
    ) {
        super(rdf4JTemplate, modelFields, migration, dtoCreator, ontology);
        this.costDao = costDao;
        this.featureDao = featureDao;
        this.monthDao = monthDao;
    }


    @Override
    public String getReadQuery() {
        return super.getReadQuery();
    }

    @Override
    public PreferenceDtoCreator getDtoCreator() {
        return (PreferenceDtoCreator) super.getDtoCreator();
    }
}
