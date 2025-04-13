package org.destirec.destirec.reasoning;

import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

@Component
public class RegionReasoning {
    private final RDF4JTemplate template;
    public RegionReasoning(RDF4JTemplate template) {
        this.template = template;
    }


    public void initialize() {
        String rdfString = template.toString();

        System.out.println(rdfString);
    }

}
