package org.destirec.destirec.rdf4j.preferences;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.attribute.AttributesCollectionMigration;
import org.destirec.destirec.rdf4j.interfaces.GenericConfig;
import org.destirec.destirec.utils.ValueContainer;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.PreferenceNames;
import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Getter
@Component
public class PreferenceConfig extends GenericConfig<PreferenceConfig.Fields> {
    private final PreferenceMigration preferenceMigration;
    private final AttributesCollectionMigration attributesCollectionMigration;

    public PreferenceConfig(PreferenceMigration preferenceMigration, AttributesCollectionMigration attributesCollectionMigration) {
        super("preference");
        this.preferenceMigration = preferenceMigration;
        this.attributesCollectionMigration = attributesCollectionMigration;
    }

    @Getter
    @AllArgsConstructor
    public enum Fields implements Field {
        HAS_COST(AttributeNames.Properties.HAS_COST.str(), true),
        HAS_MONTH(AttributeNames.Properties.HAS_MONTH.str(), true),
        HAS_FEATURE(AttributeNames.Properties.HAS_FEATURE.str(), true),
        PREFERENCE_AUTHOR(PreferenceNames.Properties.PREFERENCE_AUTHOR.getLocalName(), true);

        private final String name;
        private final boolean isRead;
    }

    @Override
    protected Fields[] getValues() {
        return Fields.values();
    }

    @Override
    public ValueContainer<IRI> getPredicate(Fields field) {
        var values = switch (field) {
            case HAS_COST -> attributesCollectionMigration.getHasCost().get();
            case HAS_FEATURE -> attributesCollectionMigration.getHasFeatures().get();
            case HAS_MONTH -> attributesCollectionMigration.getHasMonths().get();
            case PREFERENCE_AUTHOR -> PreferenceNames.Properties.PREFERENCE_AUTHOR;
            case null -> throw new IllegalArgumentException("Field is not defined");
        };

        return new ValueContainer<>(values);
    }

    @Override
    public ValueContainer<Variable> getVariable(Fields field) {
        if (field == Fields.HAS_MONTH) {
            return new ValueContainer<>(IntStream
                    .rangeClosed(0, 11)
                    .mapToObj(i -> SparqlBuilder.var(field.name() + i))
                    .collect(Collectors.toList())
            );
        }
        if (field == Fields.HAS_FEATURE) {
            return new ValueContainer<>(IntStream
                    .rangeClosed(0, RegionFeatureNames.Individuals.RegionFeature.values().length - 1)
                    .mapToObj(i -> SparqlBuilder.var(field.name() + i))
                    .collect(Collectors.toList())
            );
        }
        return new ValueContainer<>(SparqlBuilder.var(field.name()));
    }


    @Override
    public Boolean getIsOptional(Fields field) {
        if (field == Fields.HAS_MONTH || field == Fields.HAS_COST) {
            return true;
        }
        return super.getIsOptional(field);
    }

    @Override
    public ValueContainer<CoreDatatype> getType(Fields field) {
        CoreDatatype type = switch (field) {
            case PREFERENCE_AUTHOR, HAS_COST, HAS_FEATURE, HAS_MONTH -> null;
            case null -> throw new IllegalArgumentException("Field is not defined");
        };

        return new ValueContainer<>(type);
    }
}
