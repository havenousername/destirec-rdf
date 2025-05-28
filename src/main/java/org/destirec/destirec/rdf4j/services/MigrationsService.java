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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.destirec.destirec.utils.Constants.DEFAULT_GRAPH;

@Service
public class MigrationsService {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private final List<Migration> migrations = new ArrayList<>();
    private final List<OntologyDefiner> ontologies = new ArrayList<>();

    private final RDF4JTemplate template;


    @Value("${app.env.graphdb.pie_dir}")
    private String pieDir;

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

        ontologies.add(userMigration);
        ontologies.add(monthMigration);
        ontologies.add(preferenceMigration);
        ontologies.add(costMigration);
        ontologies.add(featureMigration);
        ontologies.add(attributeMigration);
        ontologies.add(attributesCollectionMigration);
        ontologies.add(qualityMigration);
        ontologies.add(regionMigration);
    }
    public void runMigrations() {
        migrations.forEach(Migration::setupAndMigrate);
        FunctionRegistry.getInstance().add(new IsPalindrome());
    }

    public void runCWARules() {
        migrations.forEach(Migration::addRuleset);
    }

    public void runOntologyDefiners() {
        ontologies.forEach(OntologyDefiner::defineOntology);
    }

    public void runPieRules() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        Path dir = Paths.get(pieDir);
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(Files::isRegularFile)
                    .filter(file -> file.endsWith(".pie"))
                    .forEach(file -> {
                        body.add(file.getFileName().toString(), new FileSystemResource(file));
                    });
//            configs/rules
        } catch (IOException filesException) {
            logger.error("Cannot get .pie files from the directory");
            throw new RuntimeException(filesException);
        }
    }
}
