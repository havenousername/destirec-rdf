package org.destirec.destirec.rdf4j;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.eclipse.rdf4j.spring.RDF4JConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.File;

@Configuration
@Import(RDF4JConfig.class)
public class ApplicationConfig {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private Repository repository;

//    @Bean
//    public Repository getRepository(
//            @Autowired @Value("${app.env.graphdb.url}") String url,
//                                   @Autowired @Value("${app.env.graphdb.default_repository}") String defaultRepository) {
//        return new HTTPRepository(url, defaultRepository);
//    }

    public ApplicationConfig() {
    }

    @Bean
    public Repository getRepository() {
        return repository;
    }


    @PostConstruct
    public void init() {
        File dataDir = new File("./database");
        if (!dataDir.exists()) {
            boolean isSuccessful = dataDir.mkdirs();
            if (!isSuccessful) {
                throw new RuntimeException("Cannot create new directory " + dataDir.getAbsolutePath());
            }
        }
        this.repository = new SailRepository(new NativeStore(dataDir));
        try {
            repository.init();
            logger.info("Initialized RDF4J repository");
        } catch (Exception exception) {
            logger.error("Cannot initialize RDF4J repository. Message: " + exception);
            throw new RuntimeException("Failed to initialize RDF4J Repository", exception);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (repository != null && repository.isInitialized()) {
            repository.shutDown();
            System.out.println("RDF4J Repository shut down successfully.");
        }
    }
}
