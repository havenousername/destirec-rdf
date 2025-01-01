package org.destirec.destirec.rdf4j.services;

import org.destirec.destirec.rdf4j.user.UserMigration;
import org.destirec.destirec.rdf4j.preferences.PreferenceMigration;
import org.destirec.destirec.rdf4j.version.SchemaVersionMigration;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.destirec.destirec.utils.Constants.DEFAULT_GRAPH;

@Service
public class MigrationsService {
    private final UserMigration userMigration;
    private final SchemaVersionMigration versionMigration;

    private final PreferenceMigration preferenceMigration;

    public MigrationsService(
            UserMigration userMigration,
            SchemaVersionMigration versionMigration,
            PreferenceMigration preferenceMigration
    ) {
        this.userMigration = userMigration;
        this.versionMigration = versionMigration;
        this.preferenceMigration = preferenceMigration;
        userMigration.setGraphName(DEFAULT_GRAPH);
        userMigration.setNamespaces(List.of(RDFS.NS, OWL.NS, RDF.NS, XSD.NS));

        versionMigration.setGraphName(DEFAULT_GRAPH);
        userMigration.setNamespaces(List.of(RDFS.NS, OWL.NS, RDF.NS, XSD.NS, SKOS.NS));

        preferenceMigration.setGraphName(DEFAULT_GRAPH);
        preferenceMigration.setNamespaces(List.of(RDFS.NS, OWL.NS, RDF.NS, XSD.NS, SKOS.NS));
    }

    public void runMigrations() {
        userMigration.setupAndMigrate();
        versionMigration.setupAndMigrate();
        preferenceMigration.setupAndMigrate();
    }
}
