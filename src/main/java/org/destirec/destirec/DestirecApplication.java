package org.destirec.destirec;

import org.destirec.destirec.rdf4j.appservices.MigrationsService;
import org.destirec.destirec.rdf4j.appservices.RdfInitializerService;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Bean
    CommandLineRunner commandLineRunner(
            MigrationsService migration,
            RdfInitializerService initializerService
    ) {
        return args -> {
            logger.info("Application has started. Next initial configuration will be set");
            migration.runMigrations();
            logger.info("Migrations have been finished. Next initialization of some basic RDF resources will be set");
            IRI version = initializerService.initializeRdfVersion();
            logger.info("RDF resource version with " + version + " iri has been be set");
        };
    }
}
