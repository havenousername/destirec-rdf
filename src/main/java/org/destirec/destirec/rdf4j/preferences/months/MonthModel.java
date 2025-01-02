package org.destirec.destirec.rdf4j.preferences.months;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericModel;
import org.destirec.destirec.rdf4j.interfaces.ModelFields;
import org.destirec.destirec.rdf4j.interfaces.container.Container;
import org.destirec.destirec.rdf4j.interfaces.container.SingularValueContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

@Component
public class MonthModel extends GenericModel<MonthModel.Fields> {
    private final MonthMigration monthMigration;

    public MonthModel(MonthMigration monthMigration) {
        super("month_id");
        this.monthMigration = monthMigration;
    }

    @Override
    public Container<IRI> getPredicate(Fields field) {
        var predicate = switch (field) {
            case RANGE -> monthMigration.getValueRange().get();
            case MONTH -> monthMigration.getMonth().get();
        };
        return new SingularValueContainer<>(predicate);
    }

    @Override
    public Container<Variable> getVariable(Fields field) {
        return new SingularValueContainer<>(SparqlBuilder.var(field.name));
    }

    @Override
    public Container<CoreDatatype> getType(Fields field) {
        var type = switch (field) {
            case MONTH -> CoreDatatype.XSD.GMONTH;
            case RANGE -> CoreDatatype.XSD.FLOAT;
        };
        return new SingularValueContainer<>(type);
    }

    @Override
    public String getResourceLocation() {
        return "/resource/month/";
    }

    @Override
    protected Fields[] getValues() {
        return Fields.values();
    }

    @AllArgsConstructor
    @Getter
    public enum Fields implements ModelFields.Field {
        MONTH("month", true),
        RANGE("monthRange", true);
        private final String name;
        private final boolean isRead;
    }
}
