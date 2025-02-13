package org.destirec.destirec.rdf4j.preferences;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PreferenceDao extends GenericDao<PreferenceConfig.Fields, PreferenceDto> {
    public PreferenceDao(
            RDF4JTemplate rdf4JTemplate,
            PreferenceConfig modelFields,
            PreferenceMigration migration,
            PreferenceDtoCreator dtoCreator
    ) {
        super(rdf4JTemplate, modelFields, migration, dtoCreator);
    }


    @Override
    public String getReadQuery() {
        return super.getReadQuery();
    }
}
