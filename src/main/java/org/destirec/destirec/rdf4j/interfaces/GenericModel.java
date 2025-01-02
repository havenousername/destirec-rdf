package org.destirec.destirec.rdf4j.interfaces;

import org.destirec.destirec.rdf4j.interfaces.container.Container;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class GenericModel<T extends Enum<T> & ModelFields.Field> implements ModelFields<T> {
    private final Variable id;
    public GenericModel(String idName) {
        id = SparqlBuilder.var("version_id");
    }

    @Override
    public Variable getId() {
        return id;
    }

    protected abstract T[] getValues();


    @Override
    public Map<T, Container<Variable>> getVariableNames() {
        return Arrays.stream(getValues())
                .collect(Collectors.toMap(Function.identity(), this::getVariable));
    }

    @Override
    public Map<T, Container<IRI>> getPredicates() {
        return Arrays.stream(getValues())
                .collect(Collectors.toMap(Function.identity(), this::getPredicate));
    }

    @Override
    public Map<T, Container<IRI>> getReadPredicates() {
        return getPredicates()
                .entrySet()
                .stream()
                .filter(fieldsIRIEntry -> fieldsIRIEntry.getKey().isRead())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<T, Container<CoreDatatype>> getTypes() {
        return Arrays.stream(getValues())
                .collect(Collectors.toMap(Function.identity(), this::getType));
    }
}
