package org.destirec.destirec.rdf4j.model.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.utils.ClassIncrement;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.*;

import java.util.Map;


public class User extends Resource<User.Fields> {
    @Getter
    @AllArgsConstructor
    public enum Fields {
        USERNAME("username"),
        EMAIL("email"),
        OCCUPATION("occupation"),
        NAME("name");

        private final String displayName;
    }

    public User() {
        super(
                "User",
                Map.ofEntries(
                        Map.entry(Fields.NAME, FOAF.NAME),
                        Map.entry(Fields.EMAIL, FOAF.MBOX),
                        Map.entry(Fields.USERNAME, FOAF.ACCOUNT_NAME),
                        Map.entry(Fields.OCCUPATION, VCARD4.ROLE)
                ),
                Map.ofEntries(
                        Map.entry(Fields.NAME, CoreDatatype.XSD.STRING),
                        Map.entry(Fields.EMAIL, CoreDatatype.XSD.STRING),
                        Map.entry(Fields.USERNAME, CoreDatatype.XSD.STRING),
                        Map.entry(Fields.OCCUPATION, CoreDatatype.XSD.STRING)
                )
        );

        ClassIncrement.getInstance().addClass(this);
        increment = ClassIncrement.getInstance().getIncrement(this);
    }

    @Override
    public void setup(ModelBuilder builder, String graphName) {
        builder
                .namedGraph(graphName)
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, FOAF.PERSON)
                .add(get(), RDFS.COMMENT, "A user of an application");
    }

    @Override
    public String getResourceLocation() {
        return "resource/user/";
    }
}
