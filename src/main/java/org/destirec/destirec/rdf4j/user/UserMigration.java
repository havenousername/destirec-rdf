package org.destirec.destirec.rdf4j.user;

import org.destirec.destirec.rdf4j.interfaces.Migration;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserMigration extends Migration {
    public UserMigration(RDF4JTemplate template) {
        super(template, "User");
    }

    @Override
    protected void setupProperties() {
        builder.add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, FOAF.PERSON)
                .add(get(), RDFS.COMMENT, "A user of an application");
    }
}
