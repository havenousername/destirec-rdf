package org.destirec.destirec.rdf4j.region.cost;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CostDao extends GenericDao<CostConfig.Fields, CostDto> {
    public CostDao(
            RDF4JTemplate rdf4JTemplate,
            CostConfig configFields,
            CostMigration migration,
            CostDtoCreator dtoCreator) {
        super(rdf4JTemplate, configFields, migration, dtoCreator);
    }

    @Override
    public CostDtoCreator getDtoCreator() {
        return (CostDtoCreator) super.getDtoCreator();
    }
}
