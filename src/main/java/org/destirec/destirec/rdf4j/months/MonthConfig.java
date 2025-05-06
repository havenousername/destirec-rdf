package org.destirec.destirec.rdf4j.months;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericConfig;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.ValueContainer;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

@Component
public class MonthConfig extends GenericConfig<MonthConfig.Fields> {
    private final MonthMigration monthMigration;

    public MonthConfig(MonthMigration monthMigration) {
        super("month_id");
        this.monthMigration = monthMigration;
    }

    @Override
    public ValueContainer<IRI> getPredicate(Fields field) {
        var predicate = switch (field) {
            case POSITION -> monthMigration.getValueRange().get();
            case MONTH_NAME -> monthMigration.getMonth().get();
            case HAS_SCORE -> AttributeNames.Properties.HAS_SCORE.rdfIri();
            case IS_ACTIVE -> AttributeNames.Properties.IS_ACTIVE.rdfIri();
        };
        return new ValueContainer<>(predicate);
    }

    @Override
    public ValueContainer<Variable> getVariable(Fields field) {
        return new ValueContainer<>(SparqlBuilder.var(field.name));
    }

    @Override
    public ValueContainer<CoreDatatype> getType(Fields field) {
        var type = switch (field) {
            case MONTH_NAME -> CoreDatatype.XSD.STRING;
            case POSITION, HAS_SCORE -> CoreDatatype.XSD.INTEGER;
            case IS_ACTIVE -> CoreDatatype.XSD.BOOLEAN;
        };
        return new ValueContainer<>(type);
    }

    @Override
    public String getResourceLocation() {
        return DESTIREC.NAMESPACE + "/resource/month/";
    }

    @Override
    protected Fields[] getValues() {
        return Fields.values();
    }

    @AllArgsConstructor
    @Getter
    public enum Fields implements Field {
        HAS_SCORE(AttributeNames.Properties.HAS_SCORE.str(), true),
        IS_ACTIVE(AttributeNames.Properties.IS_ACTIVE.str(), true),
        MONTH_NAME(AttributeNames.Properties.NAME.str(), true),
        POSITION(AttributeNames.Properties.POSITION.str(), true);
        private final String name;
        private final boolean isRead;
    }
}
