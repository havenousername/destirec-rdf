package org.destirec.destirec.rdf4j.version;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VersionDao extends GenericDao<VersionConfig.Fields, VersionDto> {
    public VersionDao(
            RDF4JTemplate rdf4JTemplate,
            VersionConfig model,
            SchemaVersionMigration migration,
            VersionDtoCreator creator,
            DestiRecOntology ontology
    ) {
        super(rdf4JTemplate, model, migration, creator, ontology);
    }
}
