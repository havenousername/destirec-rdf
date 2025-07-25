package org.destirec.destirec;

import io.github.cdimascio.dotenv.Dotenv;
import org.destirec.destirec.rdf4j.knowledgeGraph.KnowledgeGraphService;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
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
        Dotenv dotenv = Dotenv.load();
        System.setProperty("RDF_DATABASE_URL", dotenv.get("RDF_DATABASE_URL"));
        System.setProperty("REDIRECT_FRONTEND", dotenv.get("REDIRECT_FRONTEND"));
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
            logger.info("Getting all the regions to repository");
            knowledgeGraphService.addAllRegionsToRepository();
            logger.info("Fetching all the maps");
            knowledgeGraphService.fetchAllMaps();
            logger.info("Getting all the POIs to repository");
            knowledgeGraphService.addAllPOIs();
            logger.info("Updating kg ontologies with the new pois");
            knowledgeGraphService.updateKGOntologies();
//            ontology.triggerInference();
            logger.info("Setup has been finished");
        };
    }
}
