package org.destirec.destirec.rdf4j.version;

import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaVersionMigration extends IriMigration {

    protected SchemaVersionMigration(RDF4JTemplate rdf4jMethods) {
        super(rdf4jMethods, "SchemaVersion");
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.LABEL,"Schema Version Class")
                .add(get(), RDFS.DOMAIN, WIKIDATA.RDF)
                .add(get(), SKOS.RELATED, WIKIDATA.SOFTWARE_VERSION);
    }
}
