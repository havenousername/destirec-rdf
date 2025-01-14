package org.destirec.destirec.rdf4j.months;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MonthDao extends GenericDao<MonthConfig.Fields, MonthDto> {
    public MonthDao(
            RDF4JTemplate rdf4JTemplate,
            MonthConfig modelFields,
            MonthMigration migration,
            MonthDtoCreator dtoCreator
    ) {
        super(rdf4JTemplate, modelFields, migration, dtoCreator);
    }

    @Override
    public MonthDtoCreator getDtoCreator() {
        return (MonthDtoCreator) super.getDtoCreator();
    }
}
