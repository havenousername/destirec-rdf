package org.destirec.destirec.rdf4j;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.stereotype.Service;

@Service
public class Rdf4JService {
    private Repository repository;
    public void setHttpRepository(String url, String defaultRepository) {
        repository = new HTTPRepository(url, defaultRepository);
        repository.init();
    }
}
