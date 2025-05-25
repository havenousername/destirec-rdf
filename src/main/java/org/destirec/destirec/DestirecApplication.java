package org.destirec.destirec;

import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.services.KnowledgeGraphService;
import org.destirec.destirec.rdf4j.services.MigrationsService;
import org.destirec.destirec.rdf4j.services.RdfInitializerService;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DestirecApplication {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    public static void main(String[] args) {
        SpringApplication.run(DestirecApplication.class, args);
    }

    @Value("${app.env.graphdb.migrate}")
    private boolean migrate;

    @Bean
    CommandLineRunner commandLineRunner(
            MigrationsService migration,
            RdfInitializerService initializerService,
            DestiRecOntology ontology,
            KnowledgeGraphService knowledgeGraphService
    ) {
        return args -> {
            logger.info("Application has started. Next initial configuration will be set");
            var migrated = initializerService.hasVersion();
            ontology.init();
            if (migrate && !migrated) {
                migration.runOntologyDefiners();
                logger.info("Ontology definitions have been written. Next writing rules into the database");

                logger.info("Migrations will be set");
                migration.runMigrations();
                logger.info("Migrations have been finished. Next OWL rules initialization follows");
                ontology.migrate();
                logger.info("Ontology definitions have been migrated. Next some basic CWA rules will be set");
//                migration.runCWARules();
//                logger.info("CWA rules are migrated. Next initialization of some basic RDF resources will be set");


                IRI version = initializerService.initializeRdfVersion();
                logger.info("RDF resource version with {} iri has been be set", version);
            } else {
                var version = initializerService.version();
                logger.info("RDF resource version with version {} is running", version);
            }
            knowledgeGraphService.addAllRegionsToRepository();
            ontology.triggerInference();
        };
    }
}
