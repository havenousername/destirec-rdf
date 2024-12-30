package org.destirec.destirec.rdf4j.appservices;

import org.destirec.destirec.rdf4j.user.UserMigration;
import org.destirec.destirec.rdf4j.version.SchemaVersionMigration;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.destirec.destirec.utils.Constants.DEFAULT_GRAPH;

@Service
public class MigrationsService {
    private final UserMigration userMigration;
    private final SchemaVersionMigration versionMigration;

    public MigrationsService(
            UserMigration userMigration,
            SchemaVersionMigration versionMigration
    ) {
        this.userMigration = userMigration;
        this.versionMigration = versionMigration;
        userMigration.setGraphName(DEFAULT_GRAPH);
        userMigration.setNamespaces(List.of(RDFS.NS, OWL.NS, RDF.NS, XSD.NS));
    }

    public void runMigrations() {
        userMigration.setupAndMigrate();
        versionMigration.setupAndMigrate();
    }
}
