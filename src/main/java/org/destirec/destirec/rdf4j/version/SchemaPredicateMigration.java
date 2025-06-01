package org.destirec.destirec.rdf4j.version;

import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaPredicateMigration extends IriMigration {
    protected SchemaPredicateMigration(RDF4JTemplate rdf4jMethods) {
        super(rdf4jMethods, TopOntologyNames.Properties.HAS_SCHEMA_VERSION.str());
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.OBJECTPROPERTY)
                .add(get(), RDFS.LABEL,"schema version")
                .add(get(), RDFS.COMMENT, "Points to the schema version")
                .add(get(), RDFS.DOMAIN, TopOntologyNames.Classes.VERSION.rdfIri())
                .add(get(), RDFS.RANGE, XSD.FLOAT);
    }
}
