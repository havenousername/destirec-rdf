package org.destirec.destirec.rdf4j.preferences.months;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MonthDao extends GenericDao<MonthModel.Fields, MonthDto> {
    public MonthDao(
            RDF4JTemplate rdf4JTemplate,
            MonthModel modelFields,
            MonthMigration migration,
            MonthDtoCreator dtoCreator
    ) {
        super(rdf4JTemplate, modelFields, migration, dtoCreator);
    }
}
