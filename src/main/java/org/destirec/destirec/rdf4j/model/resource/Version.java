package org.destirec.destirec.rdf4j.model.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.model.predicates.DomainPredicate;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.*;

import java.util.HashMap;
import java.util.Map;

public class Version extends Resource<Version.Fields> {
    private final DomainPredicate schemaPredicate = new DomainPredicate("schemaVersion", XSD.INTEGER);

    @Getter
    @AllArgsConstructor
    public enum Fields {
        VERSION("version");

        private final String version;
    }

    public Version() {
        super("appVersion",
                new HashMap<>(),
                Map.of(Fields.VERSION, CoreDatatype.XSD.INTEGER)
        );

        schemaPredicate.setComment("Points to the version of schema");
        schemaPredicate.setLabel("schemaVersion");

        getFields().put(Fields.VERSION, schemaPredicate.get());
    }


    @Override
    public void setup(ModelBuilder builder, String graphName) {
        schemaPredicate.setup(builder, graphName);
        builder
                .namedGraph(graphName)
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.COMMENT, "Application version");
    }

    @Override
    public String getResourceLocation() {
        return "resource/v/";
    }
}
