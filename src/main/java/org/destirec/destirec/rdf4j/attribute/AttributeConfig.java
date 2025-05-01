package org.destirec.destirec.rdf4j.attribute;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.destirec.destirec.rdf4j.interfaces.GenericConfig;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

@Component
public class AttributeConfig extends GenericConfig<AttributeConfig.Fields> {
    private final AttributeMigration migration;
    public AttributeConfig(AttributeMigration migration) {
        super("attribute_id");
        this.migration = migration;
    }

    @Override
    public ValueContainer<IRI> getPredicate(Fields field) {
        var predicate = switch (field) {
            case HAS_SCORE -> migration.getHasScore();
            case IS_ACTIVE -> migration.getIsActive();
        };
        return new ValueContainer<>(predicate);
    }

    @Override
    public ValueContainer<Variable> getVariable(Fields field) {
        return new ValueContainer<>(SparqlBuilder.var(field.name()));
    }

    @Override
    public ValueContainer<CoreDatatype> getType(Fields field) {
        var type = switch (field) {
            case HAS_SCORE -> CoreDatatype.XSD.INTEGER;
            case IS_ACTIVE -> CoreDatatype.XSD.BOOLEAN;
        };
        return new ValueContainer<>(type);
    }

    @Override
    public String getResourceLocation() {
        return DESTIREC.NAMESPACE + "/resource/attribute";
    }

    @Override
    protected Fields[] getValues() {
        return Fields.values();
    }

    @AllArgsConstructor
    @Getter
    public enum Fields implements ConfigFields.Field {
        HAS_SCORE("score", true),
        IS_ACTIVE("isActive", true);
        private final String name;
        private final boolean isRead;
    }
}
