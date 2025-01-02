package org.destirec.destirec.rdf4j.services;

import org.destirec.destirec.rdf4j.preferences.months.MonthMigration;
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

    private final MonthMigration monthMigration;

    public MigrationsService(
            UserMigration userMigration,
            SchemaVersionMigration versionMigration,
            PreferenceMigration preferenceMigration, MonthMigration monthMigration
    ) {
        this.userMigration = userMigration;
        this.versionMigration = versionMigration;
        this.preferenceMigration = preferenceMigration;
        this.monthMigration = monthMigration;

        userMigration.setGraphName(DEFAULT_GRAPH);
        userMigration.setNamespaces(List.of(RDFS.NS, OWL.NS, RDF.NS, XSD.NS));

        versionMigration.setGraphName(DEFAULT_GRAPH);
        userMigration.setNamespaces(List.of(RDFS.NS, OWL.NS, RDF.NS, XSD.NS, SKOS.NS));

        preferenceMigration.setGraphName(DEFAULT_GRAPH);
        preferenceMigration.setNamespaces(List.of(RDFS.NS, OWL.NS, RDF.NS, XSD.NS, SKOS.NS));

        monthMigration.setGraphName(DEFAULT_GRAPH);
        monthMigration.setNamespaces(List.of(RDFS.NS, OWL.NS, RDF.NS, XSD.NS, SKOS.NS));
    }

    public void runMigrations() {
        userMigration.setupAndMigrate();
        versionMigration.setupAndMigrate();
        monthMigration.setupAndMigrate();
        preferenceMigration.setupAndMigrate();
    }
}
