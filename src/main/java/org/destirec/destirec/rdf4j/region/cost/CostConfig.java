package org.destirec.destirec.rdf4j.region.cost;

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
public class CostConfig extends GenericConfig<CostConfig.Fields> {
    private final CostMigration costMigration;
    public CostConfig(CostMigration costMigration) {
        super("cost_id");
        this.costMigration = costMigration;
    }

    @Override
    public ValueContainer<IRI> getPredicate(CostConfig.Fields field) {
        var predicate = switch (field) {
            case BUDGET_LEVEL -> costMigration.getBudgetLevelPredicate().get();
            case COST_PER_WEEK -> costMigration.getCostPerWeekPredicate().get();
        };

        return new ValueContainer<>(predicate);
    }

    @Override
    public ValueContainer<Variable> getVariable(CostConfig.Fields field) {
        return new ValueContainer<>(SparqlBuilder.var(field.name));
    }

    @Override
    public ValueContainer<CoreDatatype> getType(CostConfig.Fields field) {
        return new ValueContainer<>(CoreDatatype.XSD.FLOAT);
    }

    @Override
    public String getResourceLocation() {
        return DESTIREC.NAMESPACE + "/resource/cost/";
    }

    @Override
    protected Fields[] getValues() {
        return Fields.values();
    }

    @AllArgsConstructor
    @Getter
    public enum Fields implements ConfigFields.Field {
        COST_PER_WEEK("costPerWeek", true),
        BUDGET_LEVEL("budgetLevel", true);
        private final String name;
        private final boolean isRead;
    }
}
