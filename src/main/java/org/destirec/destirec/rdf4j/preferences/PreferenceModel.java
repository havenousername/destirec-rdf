package org.destirec.destirec.rdf4j.preferences;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericModel;
import org.destirec.destirec.utils.ValueContainer;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Getter
@Component
public class PreferenceModel extends GenericModel<PreferenceModel.Fields> {
    private final PreferenceMigration preferenceMigration;

    public PreferenceModel(PreferenceMigration preferenceMigration) {
        super("preference_id");
        this.preferenceMigration = preferenceMigration;
    }

    @Getter
    @AllArgsConstructor
    public enum Fields implements Field {
        IS_POPULARITY_IMPORTANT("isPopularityImportant", true),
        POPULARITY_RANGE("popularityRange", true),
        IS_PRICE_IMPORTANT("isPriceImportant", true),
        PRICE_RANGE("priceRange", true),
        PREFERENCE_AUTHOR("author", true),
        MONTHS("monthsPreference", true);

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
            case IS_POPULARITY_IMPORTANT -> preferenceMigration.getIsPopularityImportant().get();
            case PRICE_RANGE -> preferenceMigration.getPriceRange().get();
            case POPULARITY_RANGE -> preferenceMigration.getPopularityRange().get();
            case IS_PRICE_IMPORTANT -> preferenceMigration.getIsPriceImportant().get();
            case PREFERENCE_AUTHOR -> DC.CREATOR;
            case MONTHS -> preferenceMigration.getMonthPreference().get();
            case null -> throw new IllegalArgumentException("Field is not defined");
        };

        return new ValueContainer<>(values);
    }

    @Override
    public ValueContainer<Variable> getVariable(Fields field) {
        if (field == Fields.MONTHS) {
            return new ValueContainer<>(IntStream
                    .rangeClosed(0, 11)
                    .mapToObj(i -> SparqlBuilder.var(field.name() + i))
                    .collect(Collectors.toList())
            );
        }
        return new ValueContainer<>(SparqlBuilder.var(field.name()));
    }

    @Override
    public ValueContainer<CoreDatatype> getType(Fields field) {
        var type = switch (field) {
            case IS_POPULARITY_IMPORTANT, IS_PRICE_IMPORTANT -> CoreDatatype.XSD.BOOLEAN;
            case POPULARITY_RANGE, PRICE_RANGE -> CoreDatatype.XSD.FLOAT;
            case PREFERENCE_AUTHOR, MONTHS -> null;
            case null -> throw new IllegalArgumentException("Field is not defined");
        };

        return new ValueContainer<>(type);
    }

    @Override
    public String getResourceLocation() {
        return DESTIREC.NAMESPACE + "resource/preference/";
    }
}
