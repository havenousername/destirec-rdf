package org.destirec.destirec.rdf4j.userScores;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericConfig;
import org.destirec.destirec.utils.ValueContainer;
import org.destirec.destirec.utils.rdfDictionary.UserNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

@Component
public class UserHistoryConfig extends GenericConfig<UserHistoryConfig.Fields> {
    public UserHistoryConfig() {
        super("userHistory");
    }

    @Override
    protected Fields[] getValues() {
        return new Fields[0];
    }

    @Override
    public ValueContainer<IRI> getPredicate(Fields field) {
        IRI values = switch (field) {
            case HAS_TIME_FROM -> UserNames.Properties.HAS_TIME_FROM.rdfIri();
            case HAS_TIME_TO -> UserNames.Properties.HAS_TIME_TO.rdfIri();
            case HAS_USER -> UserNames.Properties.HAS_USER.rdfIri();
            case HAS_ENTITY ->  UserNames.Properties.HAS_VISITED_ENTITY.rdfIri();
            case HAS_INFLUENCE -> UserNames.Properties.HAS_INFLUENCE.rdfIri();
            case HAS_P_SCORE -> UserNames.Properties.HAS_VISITED_P_SCORE.rdfIri();
            case null -> throw new IllegalArgumentException("Field is not defined");
        };

        return new ValueContainer<>(values);
    }

    @Override
    public Boolean getIsOptional(Fields field) {
        if (field == Fields.HAS_TIME_TO || field == Fields.HAS_TIME_FROM) {
            return true;
        }
        return super.getIsOptional(field);
    }

    @Override
    public ValueContainer<Variable> getVariable(Fields field) {
        return new ValueContainer<>(SparqlBuilder.var(field.name()));
    }

    @Override
    public ValueContainer<CoreDatatype> getType(Fields field) {
        var type = switch (field) {
            case HAS_TIME_FROM, HAS_TIME_TO -> CoreDatatype.XSD.DATETIME;
            case HAS_P_SCORE -> CoreDatatype.XSD.DOUBLE;
            case HAS_ENTITY, HAS_INFLUENCE, HAS_USER -> null;
        };

        return new ValueContainer<>(type);
    }

    @Getter
    @AllArgsConstructor
    public enum Fields implements Field {
        HAS_P_SCORE("hasPScore", true),
        HAS_ENTITY("hasEntity", true),
        HAS_INFLUENCE("hasEntity", true),
        HAS_TIME_FROM("hasTimeFrom", true),
        HAS_TIME_TO("hasTimeTO", true),
        HAS_USER("hasUser", true);

        private final String name;
        private final boolean isRead;
    }
}
