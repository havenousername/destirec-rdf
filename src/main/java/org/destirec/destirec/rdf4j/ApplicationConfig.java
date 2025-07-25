package org.destirec.destirec.rdf4j;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.destirec.destirec.utils.ShortRepositoryInfo;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.eclipse.rdf4j.spring.RDF4JConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.util.Map;

@EnableCaching
@Configuration
@Import(RDF4JConfig.class)
public class ApplicationConfig {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private Repository repository;

    @Value("${app.env.graphdb.url}")
    private String url;

    @Value("${app.env.graphdb.default_repository}")
    private String repositoryName;

    @Value("${app.env.graphdb.is_remote}")
    private Boolean isRemote;

    public ApplicationConfig() {}

    @Bean
    public Repository getRepository() {
        return repository;
    }

    @Bean
    public String getRepositoryName() {
        return repositoryName;
    }

    @Bean
    public ShortRepositoryInfo getRepositoryInfo() {
        return new ShortRepositoryInfo(isRemote, repository);
    }

    private void configSystem() {
        System.setProperty("org.eclipse.rdf4j.repository.debug", "false");
    }

    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }


    @PostConstruct
    public void init() {
        configSystem();
        if (isRemote) {
            var repo = new HTTPRepository(url, repositoryName);
            repo.setAdditionalHttpHeaders(Map.of("User-Agent", "Destirec/1.0 (+https://destination-finder-production.up.railway.app; contact:cristea.andrei997@gmail.com)"));
            logger.info("Connected to RDF repository with the URI {}", url);
            System.out.println(repo.getConnection().isActive());
            repository = repo;
        } else {
            createLocalRepository();
        }
    }

    public void createLocalRepository() {
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

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
