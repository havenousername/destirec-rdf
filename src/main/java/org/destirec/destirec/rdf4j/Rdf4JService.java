package org.destirec.destirec.rdf4j;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class Rdf4JService {
    private Repository repository;

    Logger logger = Logger.getLogger(String.valueOf(Rdf4JService.class));

    public void setHttpRepository(String url, String defaultRepository) {
        repository = new HTTPRepository(url, defaultRepository);
        repository.init();
    }
}
