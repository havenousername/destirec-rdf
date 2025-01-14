package org.destirec.destirec.rdf4j.services;

import org.destirec.destirec.rdf4j.interfaces.Migration;
import org.destirec.destirec.rdf4j.months.MonthMigration;
import org.destirec.destirec.rdf4j.region.cost.CostMigration;
import org.destirec.destirec.rdf4j.region.feature.FeatureMigration;
import org.destirec.destirec.rdf4j.user.UserMigration;
import org.destirec.destirec.rdf4j.user.preferences.PreferenceMigration;
import org.destirec.destirec.rdf4j.version.SchemaVersionMigration;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.destirec.destirec.utils.Constants.DEFAULT_GRAPH;

@Service
public class MigrationsService {
    private final ArrayList<Migration> migrations = new ArrayList<>();

    public MigrationsService(
            UserMigration userMigration,
            SchemaVersionMigration versionMigration,
            PreferenceMigration preferenceMigration,
            MonthMigration monthMigration,
            CostMigration costMigration,
            FeatureMigration featureMigration
    ) {
        migrations.add(userMigration);
        migrations.add(versionMigration);
        migrations.add(preferenceMigration);
        migrations.add(monthMigration);
        migrations.add(costMigration);
        migrations.add(featureMigration);

        migrations.forEach(migration -> {
            migration.setGraphName(DEFAULT_GRAPH);
            migration.setNamespaces(List.of(RDFS.NS, OWL.NS, RDF.NS, XSD.NS));
        });
    }

    public void runMigrations() {
        migrations.forEach(Migration::setupAndMigrate);
    }
}
