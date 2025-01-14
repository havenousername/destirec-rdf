package org.destirec.destirec.rdf4j.region;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericConfig;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class RegionConfig extends GenericConfig<RegionConfig.Fields> {
    private final RegionMigration regionMigration;
    public RegionConfig(RegionMigration regionMigration) {
        super("region_id");
        this.regionMigration = regionMigration;
    }

    @Override
    public ValueContainer<IRI> getPredicate(Fields field) {
        var values = switch (field) {
            case FEATURES -> regionMigration.getHasFeatures().get();
            case MONTHS -> regionMigration.getHasMonths().get();
            case COST -> regionMigration.getHasCost().get();
            case null -> throw new IllegalArgumentException("Field is not defined");
        };
        return new ValueContainer<>(values);
    }

    @Override
    public ValueContainer<Variable> getVariable(Fields field) {
        switch (field) {
            case MONTHS, FEATURES -> {
                return new ValueContainer<>(IntStream
                        .rangeClosed(0, 11)
                        .mapToObj(i -> SparqlBuilder.var(field.name() + i))
                        .collect(Collectors.toList()));
            }
            case COST -> {
                return new ValueContainer<>(SparqlBuilder.var(field.name()));
            }
            case null -> throw new IllegalArgumentException("Field is not defined");
        }
    }

    @Override
    public ValueContainer<CoreDatatype> getType(Fields field) {
        return new ValueContainer<>(null);
    }

    @Override
    public String getResourceLocation() {
        return DESTIREC.NAMESPACE + "resource/region/";
    }

    @Override
    protected Fields[] getValues() {
        return Fields.values();
    }

    @Getter
    @AllArgsConstructor
    public enum Fields implements Field {
        FEATURES("features", true),
        MONTHS("months", true),
        COST("costs", true);

        private final String name;
        private final boolean isRead;
    }

}
