package org.destirec.destirec.rdf4j.services;

import org.destirec.destirec.rdf4j.attribute.AttributeMigration;
import org.destirec.destirec.rdf4j.attribute.AttributesCollectionMigration;
import org.destirec.destirec.rdf4j.attribute.QualityMigration;
import org.destirec.destirec.rdf4j.functions.IsPalindrome;
import org.destirec.destirec.rdf4j.interfaces.Migration;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.months.MonthMigration;
import org.destirec.destirec.rdf4j.ontology.TopOntologyMigration;
import org.destirec.destirec.rdf4j.preferences.PreferenceMigration;
import org.destirec.destirec.rdf4j.region.RegionMigration;
import org.destirec.destirec.rdf4j.region.cost.CostMigration;
import org.destirec.destirec.rdf4j.region.feature.FeatureMigration;
import org.destirec.destirec.rdf4j.user.UserMigration;
import org.destirec.destirec.rdf4j.version.SchemaVersionMigration;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.algebra.evaluation.function.FunctionRegistry;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.destirec.destirec.utils.Constants.DEFAULT_GRAPH;

@Service
public class MigrationsService {
    private final List<Migration> migrations = new ArrayList<>();
    private final List<OntologyDefiner> ontologies = new ArrayList<>();

    private final RDF4JTemplate template;

    public MigrationsService(
            UserMigration userMigration,
            RegionMigration regionMigration,
            SchemaVersionMigration versionMigration,
            PreferenceMigration preferenceMigration,
            MonthMigration monthMigration,
            CostMigration costMigration,
            FeatureMigration featureMigration,
            TopOntologyMigration topOntologyMigration,
            AttributeMigration attributeMigration,
            AttributesCollectionMigration attributesCollectionMigration,
            QualityMigration qualityMigration,
            RDF4JTemplate template

    ) {
        this.template = template;
        migrations.add(topOntologyMigration);
        migrations.add(userMigration);
        migrations.add(versionMigration);
        migrations.add(preferenceMigration);
        migrations.add(monthMigration);
        migrations.add(costMigration);
        migrations.add(featureMigration);
        migrations.add(attributeMigration);
        migrations.add(attributesCollectionMigration);
        migrations.add(qualityMigration);
        migrations.add(regionMigration);

        migrations.forEach(migration -> {
            migration.setGraphName(DEFAULT_GRAPH);
            migration.setNamespaces(List.of(RDFS.NS, OWL.NS, RDF.NS, XSD.NS));
        });

//        ontologies.add(userMigration);
//        ontologies.add(monthMigration);
//        ontologies.add(attributeMigration);
//        ontologies.add(preferenceMigration);
//        ontologies.add(monthMigration);
//        ontologies.add(costMigration);
//        ontologies.add(featureMigration);
//        ontologies.add(attributeMigration);
//        ontologies.add(attributesCollectionMigration);
//        ontologies.add(qualityMigration);
        ontologies.add(regionMigration);
    }

    public void runCleanup() {
        String update = """
                    PREFIX ont: <http://destirec.com/ontology/>
                    DELETE {
                        ?s ?p ?o .
                    } WHERE {
                        ?s ?p ?o .
                        FILTER(
                        REGEX(STR(?s), "^http://destirec.com/ontology/") ||
                        REGEX(STR(?p), "^http://destirec.com/ontology/") ||
                        REGEX(STR(?o), "^http://destirec.com/ontology/"))
                    }
                """;

        template.consumeConnection(connection -> {
            connection.prepareUpdate(update).execute();
        });
    }

    public void runMigrations() {
        migrations.forEach(Migration::setupAndMigrate);
        FunctionRegistry.getInstance().add(new IsPalindrome());
    }

    public void runOntologyDefiners() {
        ontologies.forEach(OntologyDefiner::defineOntology);
    }
}
