package org.destirec.destirec.rdf4j.version;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.ModelFields;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VersionModel implements ModelFields<VersionModel.Fields> {
    private final Variable id = SparqlBuilder.var("version_id");
    private final SchemaPredicateMigration schemaPredicate;
    public VersionModel(SchemaPredicateMigration schemaPredicate) {
        this.schemaPredicate = schemaPredicate;
    }
    @Override
    public Variable getId() {
        return id;
    }

    @Override
    public Map<Fields, Variable> getVariableNames() {
        return Map.of(Fields.VERSION, getVariable(Fields.VERSION));
    }

    @Override
    public Map<Fields, IRI> getPredicates() {
        return Map.of(Fields.VERSION, getPredicate(Fields.VERSION));
    }

    @Override
    public Map<Fields, IRI> getReadPredicates() {
        return getPredicates();
    }

    @Override
    public Map<Fields, CoreDatatype> getTypes() {
        return Map.of(Fields.VERSION, getType(Fields.VERSION));
    }

    @Override
    public IRI getPredicate(Fields field) {
        return schemaPredicate.get();
    }

    @Override
    public Variable getVariable(Fields field) {
        return SparqlBuilder.var(field.name);
    }

    @Override
    public CoreDatatype getType(Fields field) {
        return CoreDatatype.XSD.FLOAT;
    }

    @Override
    public String getResourceLocation() {
        return "resource/version/";
    }

    @Getter
    @AllArgsConstructor
    public enum Fields implements ModelFields.Field {
        VERSION("version", true);

        private final String name;
        private final boolean isRead;
    }
}
