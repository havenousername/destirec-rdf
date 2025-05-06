package org.destirec.destirec.rdf4j.region.feature;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
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
public class FeatureConfig extends GenericConfig<FeatureConfig.Fields> {
    public FeatureConfig() {
        super("feature_id");
    }

    @Override
    public ValueContainer<IRI> getPredicate(Fields field) {
        var predicate = switch (field) {
            case HAS_SCORE -> AttributeNames.Properties.HAS_SCORE.rdfIri();
            case IS_ACTIVE -> AttributeNames.Properties.IS_ACTIVE.rdfIri();
            case HAS_REGION_FEATURE -> AttributeNames.Properties.HAS_REGION_FEATURE.rdfIri();
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
            case HAS_SCORE -> CoreDatatype.XSD.INTEGER;
            case IS_ACTIVE -> CoreDatatype.XSD.BOOLEAN;
            case HAS_REGION_FEATURE -> null;
        };
        return new ValueContainer<>(type);
    }

    @Override
    public String getResourceLocation() {
        return DESTIREC.NAMESPACE + "/resource/feature/";
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
        HAS_REGION_FEATURE(AttributeNames.Properties.HAS_REGION_FEATURE.str(), true);
        private final String name;
        private final boolean isRead;
    }
}
