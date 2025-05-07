package org.destirec.destirec.rdf4j.region;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.destirec.destirec.rdf4j.attribute.AttributesCollectionMigration;
import org.destirec.destirec.rdf4j.interfaces.GenericConfig;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class RegionConfig extends GenericConfig<RegionConfig.Fields> {
    private final RegionMigration regionMigration;
    private final AttributesCollectionMigration collectionMigration;

    @Setter
    private List<String> featureNames;
    public RegionConfig(RegionMigration regionMigration, AttributesCollectionMigration collectionMigration) {
        super("region_id");
        this.regionMigration = regionMigration;
        this.collectionMigration = collectionMigration;
    }

    @Override
    public ValueContainer<IRI> getPredicate(Fields field) {
        var values = switch (field) {
            case FEATURES -> collectionMigration.getHasFeatures().get();
            case MONTHS -> collectionMigration.getHasMonths().get();
            case COST -> collectionMigration.getHasCost().get();
            case NAME -> FOAF.NAME;
            case PARENT_REGION -> GEO.sfWithin;
            case null -> throw new IllegalArgumentException("Field is not defined");
        };
        return new ValueContainer<>(values);
    }

    @Override
    public ValueContainer<Variable> getVariable(Fields field) {
        if (field == Fields.FEATURES && featureNames != null) {
            return new ValueContainer<>(featureNames
                    .stream()
                    .map(SparqlBuilder::var)
                    .toList()
            );
        }
        switch (field) {
            case MONTHS, FEATURES -> {
                return new ValueContainer<>(IntStream
                        .rangeClosed(0, 11)
                        .mapToObj(i -> SparqlBuilder.var(field.name() + i))
                        .collect(Collectors.toList()));
            }
            case COST, NAME, PARENT_REGION -> {
                return new ValueContainer<>(SparqlBuilder.var(field.name()));
            }
            case null -> throw new IllegalArgumentException("Field is not defined");
        }
    }

    @Override
    public ValueContainer<CoreDatatype> getType(Fields field) {
        if (field == Fields.NAME) {
            return new ValueContainer<>(CoreDatatype.XSD.STRING);
        }
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
        NAME("name", true),
        PARENT_REGION("parentRegion", true),
        FEATURES("features", true),
        MONTHS("months", true),
        COST("costs", true);

        private final String name;
        private final boolean isRead;
    }

}
