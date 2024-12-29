package org.destirec.destirec.rdf4j.beans;

import lombok.Getter;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;

@Getter
public class RdfBean {
    private final RDF4JTemplate rdf4JTemplate;

    public RdfBean(RDF4JTemplate template) {
        rdf4JTemplate = template;
    }
}
