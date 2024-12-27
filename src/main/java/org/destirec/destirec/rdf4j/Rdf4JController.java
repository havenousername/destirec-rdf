package org.destirec.destirec.rdf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
public class Rdf4JController {
    private final Rdf4JService rdf4JService;
    @Autowired
    public Rdf4JController(
            @Value("${app.env.graphdb.url}") String url,
            @Value("${app.env.graphdb.default_repository}") String defaultRepository,
            Rdf4JService service
    ) {
        rdf4JService = service;
        rdf4JService.setHttpRepository(url, defaultRepository);
    }
}
