package org.destirec.destirec.rdf4j.preferences;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.ModelFields;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Getter
@Component
public class PreferenceModel implements ModelFields<PreferenceModel.Fields> {
    private final Variable id = SparqlBuilder.var("preference_id");


    private final PreferenceMigration preferenceMigration;

    public PreferenceModel(PreferenceMigration preferenceMigration) {
        this.preferenceMigration = preferenceMigration;
    }

    @Getter
    @AllArgsConstructor
    public enum Fields implements ModelFields.Field {
        IS_POPULARITY_IMPORTANT("isPopularityImportant", true),
        POPULARITY_RANGE("popularityRange", true),
        IS_PRICE_IMPORTANT("isPriceImportant", true),
        PRICE_RANGE("priceRange", true),

        PREFERENCE_AUTHOR("author", true);

        private final String name;
        private final boolean isRead;
    }

    @Override
    public Variable getId() {
        return id;
    }

    @Override
    public Map<Fields, Variable> getVariableNames() {
        return Arrays.stream(Fields.values())
                .collect(Collectors.toMap(Function.identity(), this::getVariable));
    }

    @Override
    public Map<Fields, IRI> getPredicates() {
        return Arrays.stream(Fields.values())
                .collect(Collectors.toMap(Function.identity(), this::getPredicate));
    }

    @Override
    public Map<Fields, IRI> getReadPredicates() {
        return getPredicates()
                .entrySet()
                .stream()
                .filter(fieldsIRIEntry -> fieldsIRIEntry.getKey().isRead())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<Fields, CoreDatatype> getTypes() {
        return Arrays.stream(Fields.values())
                .collect(Collectors.toMap(Function.identity(), this::getType));
    }

    @Override
    public IRI getPredicate(Fields field) {
        return switch (field) {
            case IS_POPULARITY_IMPORTANT -> preferenceMigration.isPopularityImportant.get();
            case PRICE_RANGE -> preferenceMigration.priceRange.get();
            case POPULARITY_RANGE -> preferenceMigration.popularityRange.get();
            case IS_PRICE_IMPORTANT -> preferenceMigration.isPriceImportant.get();
            case PREFERENCE_AUTHOR -> DC.CREATOR;
            case null -> throw new IllegalArgumentException("Field is not defined");
        };
    }

    @Override
    public Variable getVariable(Fields field) {
        return SparqlBuilder.var(field.name());
    }

    @Override
    public CoreDatatype getType(Fields field) {
        return switch (field) {
            case IS_POPULARITY_IMPORTANT, IS_PRICE_IMPORTANT -> CoreDatatype.XSD.BOOLEAN;
            case POPULARITY_RANGE, PRICE_RANGE -> CoreDatatype.XSD.FLOAT;
            case PREFERENCE_AUTHOR -> CoreDatatype.XSD.ANYURI;
            case null -> throw new IllegalArgumentException("Field is not defined");
        };
    }

    @Override
    public String getResourceLocation() {
        return DESTIREC.NAMESPACE + "resource/preference/";
    }
}
