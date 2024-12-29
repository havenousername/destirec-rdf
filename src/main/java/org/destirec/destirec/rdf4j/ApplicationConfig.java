package org.destirec.destirec.rdf4j;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.spring.RDF4JConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(RDF4JConfig.class)
public class ApplicationConfig {

    @Bean
    public Repository getRepository(@Autowired @Value("${app.env.graphdb.url}") String url,
                                   @Autowired @Value("${app.env.graphdb.default_repository}") String defaultRepository) {
        return new HTTPRepository(url, defaultRepository);
    }
}
