package org.destirec.destirec.rdf4j.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericModel;
import org.destirec.destirec.rdf4j.interfaces.ModelFields;
import org.destirec.destirec.rdf4j.interfaces.container.Container;
import org.destirec.destirec.rdf4j.interfaces.container.SingularValueContainer;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.VCARD4;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

@Getter
@Component
public class UserModel extends GenericModel<UserModel.Fields> {
    public UserModel() {
        super("user_id");
    }

    @Override
    public String getResourceLocation() {
        return DESTIREC.NAMESPACE + "resource/user/";
    }

    @Override
    public Container<IRI> getPredicate(Fields field) {
         var predicate = switch (field) {
            case USERNAME -> FOAF.ACCOUNT_NAME;
            case EMAIL -> FOAF.MBOX;
            case OCCUPATION -> VCARD4.ROLE;
            case NAME -> FOAF.NAME;
        };
         return new SingularValueContainer<>(predicate);
    }

    @Override
    public Container<Variable> getVariable(Fields field) {
        return new SingularValueContainer<>(SparqlBuilder.var(field.name));
    }

    @Override
    public Container<CoreDatatype> getType(Fields field) {
        return new SingularValueContainer<>(CoreDatatype.XSD.STRING);
    }

    @Override
    protected Fields[] getValues() {
        return Fields.values();
    }

    @Getter
    @AllArgsConstructor
    public enum Fields implements ModelFields.Field {
        NAME("name", true),
        USERNAME("username", true),
        EMAIL("email", true),
        OCCUPATION("occupation", true);

        private final String name;
        private final boolean isRead;
    }
}
