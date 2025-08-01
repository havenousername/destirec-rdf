package org.destirec.destirec.rdf4j.region;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.destirec.destirec.rdf4j.attribute.AttributesCollectionMigration;
import org.destirec.destirec.rdf4j.interfaces.GenericConfig;
import org.destirec.destirec.utils.ValueContainer;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class RegionConfig extends GenericConfig<RegionConfig.Fields> {
    private final AttributesCollectionMigration collectionMigration;
    @Setter
    private List<String> featureNames;
    public RegionConfig(AttributesCollectionMigration collectionMigration) {
        super("region");
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
            case SOURCE ->  DC.SOURCE;
            case REGION_TYPE -> RegionNames.Properties.HAS_LEVEL.rdfIri();
            case ISO -> SKOS.NOTATION;
            case GEO_SHAPE -> GEO.Geometry;
            case OSM ->  RegionNames.Properties.HAS_OSM_ID.rdfIri();
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
            case COST, NAME, PARENT_REGION, SOURCE, REGION_TYPE, ISO, GEO_SHAPE, OSM -> {
                return new ValueContainer<>(SparqlBuilder.var(field.name()));
            }
            case null -> throw new IllegalArgumentException("Field is not defined");
        }
    }

    @Override
    public ValueContainer<CoreDatatype> getType(Fields field) {
        if (field == Fields.NAME || field == Fields.ISO || field == Fields.OSM) {
            return new ValueContainer<>(CoreDatatype.XSD.STRING);
        }
        return new ValueContainer<>(null);
    }

    @Override
    public Boolean getIsOptional(Fields field) {
        return switch (field) {
            case NAME -> false;
            case PARENT_REGION, FEATURES, MONTHS, SOURCE, COST, REGION_TYPE, ISO, GEO_SHAPE, OSM -> true;
        };
    }

    @Override
    protected Fields[] getValues() {
        return Fields.values();
    }

    @Getter
    @AllArgsConstructor
    public enum Fields implements Field {
        NAME(RegionNames.Properties.NAME.str(), true),
        PARENT_REGION("parentRegion", true),
        FEATURES("features", true),
        MONTHS("months", true),
        SOURCE("sourceIRI", true),
        COST("costs", true),

        REGION_TYPE("regionType", true),

        // new types
        ISO("isoName", true),
        GEO_SHAPE("shape", true),
        OSM("osm", true);

        private final String name;
        private final boolean isRead;
    }

}
