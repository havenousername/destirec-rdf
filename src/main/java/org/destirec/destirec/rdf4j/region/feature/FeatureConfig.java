package org.destirec.destirec.rdf4j.region.feature;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.destirec.destirec.rdf4j.interfaces.GenericConfig;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
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
            case KIND -> SKOS.CONCEPT;
            case VALUE -> OWL.HASVALUE;
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
            case KIND -> CoreDatatype.XSD.STRING;
            case VALUE -> CoreDatatype.XSD.FLOAT;
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
        VALUE("value", true),
        KIND("kind", true);
        private final String name;
        private final boolean isRead;
    }
}
