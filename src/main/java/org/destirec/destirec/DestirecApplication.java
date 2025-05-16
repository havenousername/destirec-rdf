package org.destirec.destirec;

import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.services.MigrationsService;
import org.destirec.destirec.rdf4j.services.RdfInitializerService;
import org.eclipse.rdf4j.model.IRI;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.ByteArrayOutputStream;

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
            DestiRecOntology ontology
    ) {
        return args -> {
            logger.info("Application has started. Next initial configuration will be set");
            var migrated = false;
            if (migrate && !migrated) {
                migration.runCleanup();
                migration.runOntologyDefiners();
                logger.info("Ontology definitions have been written. Next writing rules into the database");

                logger.info("Migrations will be set");
                migration.runMigrations();
                logger.info("Migrations have been finished. Next OWL rules initialization follows");
                ontology.migrate();
                logger.info("Ontology definitions have been migrated. Next initialization of some basic RDF resources will be set");
                IRI version = initializerService.initializeRdfVersion();
                logger.info("RDF resource version with " + version + " iri has been be set");
//
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ontology.getManager().saveOntology(ontology.getOntology(), new TurtleDocumentFormat(), outputStream);
                String output = outputStream.toString();
                System.out.println("End");
            } else {
                var version = initializerService.version();
                logger.info("RDF resource version with version " + version  + " is running");
            }
        };
    }
}
