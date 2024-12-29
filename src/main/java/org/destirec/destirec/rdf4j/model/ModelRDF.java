package org.destirec.destirec.rdf4j.model;

import lombok.Getter;
import org.destirec.destirec.rdf4j.model.resource.User;
import org.destirec.destirec.rdf4j.model.resource.UserPreferences;
import org.destirec.destirec.rdf4j.model.resource.Version;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Model RDF defined URI's for the group of triples that define a user
 * and their connections between each other. The result of the class
 * is a TURTLE format text
 */
@Getter
public class ModelRDF {
    private final ModelBuilder builder;

    private final User userClass = new User();

    private final UserPreferences userPreferences = new UserPreferences(userClass.getIri());

    private final Version schemaVersion = new Version();

    public ModelRDF(ModelBuilder builder) {
        this.builder = builder;

        setup();
    }

    public ModelRDF() {
        builder = new ModelBuilder();
        setup();
    }

    private void setup() {
        setupBuilderNamespace(builder);
        setupVersion();
        setupUser();
        setupPreferences();
    }

    private void setupVersion() {
        schemaVersion.setup(builder, getGraphName());
        schemaVersion.createResource(builder, getGraphName());
    }


    private void setupBuilderNamespace(ModelBuilder builder) {
        builder
                .setNamespace(RDFS.NS)
                .setNamespace(FOAF.NS)
                .setNamespace(RDF.NS)
                .setNamespace(OWL.NS)
                .setNamespace(WIKIDATA.NS)
                .setNamespace(TIME.NS)
                .setNamespace(XSD.NS);
    }

    private void setupUser() {
        userClass.setup(builder, getGraphName());
    }

    private void setupPreferences() {
        // setup preferences class inside user
        userPreferences.setup(builder, getGraphName());
    }
    
    public String getGraphName() {
        return DESTIREC.NAMESPACE + ":defaultGraph";
    }

    @Override
    public String toString() {
        return toString(RDFFormat.TURTLE);
    }

    public Model getModel() {
        return builder.build();
    }

    public String toString(RDFFormat format) {
        try (Writer writer = new StringWriter()) {
            Rio.write(builder.build(), writer, format);
            return writer.toString();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
        return "";
    }
}