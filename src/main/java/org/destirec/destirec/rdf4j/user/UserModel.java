package org.destirec.destirec.rdf4j.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.destirec.destirec.rdf4j.interfaces.ModelFields;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.VCARD4;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@Component
public class UserModel implements ModelFields<UserModel.Fields> {
    private final Variable id = SparqlBuilder.var("user_id");

    private final Map<UserModel.Fields, Variable> variableNames = Arrays
            .stream(UserModel.Fields.values()).collect(Collectors.toMap(Function.identity(), this::getVariable));

    private final Map<UserModel.Fields, IRI> predicates = Arrays
            .stream(UserModel.Fields.values()).collect(Collectors.toMap(Function.identity(), this::getPredicate));

    private final Map<UserModel.Fields, CoreDatatype> types = Arrays
            .stream(UserModel.Fields.values()).collect(Collectors.toMap(Function.identity(), this::getType));

    @Override
    public String getResourceLocation() {
        return DESTIREC.NAMESPACE + "resource/user/";
    }

    @Override
    public Map<Fields, IRI> getReadPredicates() {
        return predicates.entrySet().stream()
                .filter(fieldsIRIEntry -> fieldsIRIEntry.getKey().isRead())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public IRI getPredicate(Fields field) {
         return switch (field) {
            case USERNAME -> FOAF.ACCOUNT_NAME;
            case EMAIL -> FOAF.MBOX;
            case OCCUPATION -> VCARD4.ROLE;
            case NAME -> FOAF.NAME;
        };
    }

    @Override
    public Variable getVariable(Fields field) {
        return SparqlBuilder.var(field.name);
    }

    @Override
    public CoreDatatype getType(Fields field) {
        return CoreDatatype.XSD.STRING;
    }

    @Getter
    @AllArgsConstructor
    public enum Fields implements ModelFields.Field {
        NAME("name", true),
        USERNAME("username", false),
        EMAIL("email", true),
        OCCUPATION("occupation", false);

        private final String name;
        private final boolean isRead;
    }
}
