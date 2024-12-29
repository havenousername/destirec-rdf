package org.destirec.destirec.rdf4j.model.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.model.predicates.DomainPredicate;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import java.util.HashMap;
import java.util.Map;

import static org.destirec.destirec.utils.Constants.RDF_VERSION;

public class Version extends Resource<Version.Fields> {
    private final DomainPredicate schemaVersionClass = new DomainPredicate("SchemaVersion");
    private final DomainPredicate schemaPredicate = new DomainPredicate("hasSchemaVersion", schemaVersionClass.get());

    @Getter
    @AllArgsConstructor
    public enum Fields {
        VERSION("version");

        private final String version;
    }

    public Version() {
        super("schemaVersion",
                new HashMap<>(),
                Map.of(Fields.VERSION, CoreDatatype.XSD.INTEGER)
        );

        schemaVersionClass.setLabel("Schema Version");

        schemaPredicate.setComment("Points to the version of schema");
        schemaPredicate.setLabel("schemaVersion");
        schemaPredicate.setType(OWL.OBJECTPROPERTY);

        getFields().put(Fields.VERSION, schemaPredicate.get());
    }


    @Override
    public void setup(ModelBuilder builder, String graphName) {
        schemaVersionClass.setup(builder, graphName);
        schemaPredicate.setup(builder, graphName);
        builder
                .namedGraph(graphName)
                .add(get(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(get(), RDFS.COMMENT, "Application version");
    }

    @Override
    public void createResource(ModelBuilder builder, String graphName) {
        builder
                .namedGraph(graphName)
                .subject(get());
        builder.add(schemaPredicate.get(), RDF_VERSION);
    }

    @Override
    public String getResourceLocation() {
        return "";
    }
}
