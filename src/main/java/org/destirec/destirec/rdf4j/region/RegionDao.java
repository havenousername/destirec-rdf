package org.destirec.destirec.rdf4j.region;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RegionDao extends GenericDao<RegionConfig.Fields, RegionDto> {
    public RegionDao(
            RDF4JTemplate rdf4JTemplate,
            RegionConfig configFields,
            RegionMigration migration,
            RegionDtoCreator dtoCreator) {
        super(rdf4JTemplate, configFields, migration, dtoCreator);
    }
}
