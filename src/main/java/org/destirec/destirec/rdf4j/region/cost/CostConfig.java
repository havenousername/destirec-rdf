package org.destirec.destirec.rdf4j.region.cost;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.destirec.destirec.rdf4j.interfaces.GenericConfig;
import org.destirec.destirec.utils.ValueContainer;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

@Component
public class CostConfig extends GenericConfig<CostConfig.Fields> {
    private final CostMigration costMigration;
    public CostConfig(CostMigration costMigration) {
        super("cost");
        this.costMigration = costMigration;
    }

    @Override
    public ValueContainer<IRI> getPredicate(CostConfig.Fields field) {
        var predicate = switch (field) {
            case BUDGET_LEVEL -> costMigration.getBudgetLevelPredicate().get();
            case COST_PER_WEEK -> costMigration.getCostPerWeekPredicate().get();
            case HAS_SCORE -> AttributeNames.Properties.HAS_SCORE.rdfIri();
            case IS_ACTIVE -> AttributeNames.Properties.IS_ACTIVE.rdfIri();
        };

        return new ValueContainer<>(predicate);
    }

    @Override
    public ValueContainer<Variable> getVariable(CostConfig.Fields field) {
        return new ValueContainer<>(SparqlBuilder.var(field.name));
    }

    @Override
    public ValueContainer<CoreDatatype> getType(CostConfig.Fields field) {
        var type = switch (field) {
            case HAS_SCORE, BUDGET_LEVEL, COST_PER_WEEK -> CoreDatatype.XSD.INTEGER;
            case IS_ACTIVE -> CoreDatatype.XSD.BOOLEAN;
        };
        return new ValueContainer<>(type);
    }

    @Override
    protected Fields[] getValues() {
        return Fields.values();
    }

    @AllArgsConstructor
    @Getter
    public enum Fields implements ConfigFields.Field {
        HAS_SCORE(AttributeNames.Properties.HAS_SCORE.str(), true),
        IS_ACTIVE(AttributeNames.Properties.IS_ACTIVE.str(), true),
        COST_PER_WEEK(AttributeNames.Properties.HAS_SCORE_PER_WEEK.str(), true),
        BUDGET_LEVEL(AttributeNames.Properties.HAS_BUDGET_LEVEL.str(), true);

        private final String name;
        private final boolean isRead;
    }
}
