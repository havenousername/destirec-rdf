package org.destirec.destirec.rdf4j.version;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VersionDao extends GenericDao<VersionModel.Fields, VersionDto> {
    public VersionDao(
            RDF4JTemplate rdf4JTemplate,
            VersionModel model,
            SchemaVersionMigration migration,
            VersionDtoCreator creator
    ) {
        super(rdf4JTemplate, model, migration, creator);
    }
}
