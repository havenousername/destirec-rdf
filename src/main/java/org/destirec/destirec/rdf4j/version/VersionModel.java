package org.destirec.destirec.rdf4j.version;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericModel;
import org.destirec.destirec.rdf4j.interfaces.ModelFields;
import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

@Component
public class VersionModel extends GenericModel<VersionModel.Fields> {
    private final SchemaPredicateMigration schemaPredicate;
    public VersionModel(SchemaPredicateMigration schemaPredicate) {
        super("version_id");
        this.schemaPredicate = schemaPredicate;
    }


    @Override
    public ValueContainer<IRI> getPredicate(Fields field) {
        return new ValueContainer<>(schemaPredicate.get());
    }

    @Override
    public ValueContainer<Variable> getVariable(Fields field) {
        return new ValueContainer<>(SparqlBuilder.var(field.name));
    }

    @Override
    public ValueContainer<CoreDatatype> getType(Fields field) {
        return new ValueContainer<>(CoreDatatype.XSD.FLOAT);
    }

    @Override
    public String getResourceLocation() {
        return "resource/version/";
    }

    @Override
    protected Fields[] getValues() {
        return Fields.values();
    }

    @Getter
    @AllArgsConstructor
    public enum Fields implements ModelFields.Field {
        VERSION("version", true);

        private final String name;
        private final boolean isRead;
    }
}
