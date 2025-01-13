package org.destirec.destirec.rdf4j.interfaces;

import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class GenericConfig<T extends Enum<T> & ConfigFields.Field> implements ConfigFields<T> {
    private final Variable id;
    public GenericConfig(String idName) {
        id = SparqlBuilder.var(idName);
    }

    @Override
    public Variable getId() {
        return id;
    }

    protected abstract T[] getValues();


    @Override
    public Map<T, ValueContainer<Variable>> getVariableNames() {
        return Arrays.stream(getValues())
                .collect(Collectors.toMap(Function.identity(), this::getVariable));
    }

    @Override
    public Map<T, ValueContainer<IRI>> getPredicates() {
        return Arrays.stream(getValues())
                .collect(Collectors.toMap(Function.identity(), this::getPredicate));
    }

    @Override
    public Map<T, ValueContainer<IRI>> getReadPredicates() {
        return getPredicates()
                .entrySet()
                .stream()
                .filter(fieldsIRIEntry -> fieldsIRIEntry.getKey().isRead())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<T, ValueContainer<CoreDatatype>> getTypes() {
        return Arrays.stream(getValues())
                .collect(Collectors.toMap(Function.identity(), this::getType));
    }

    @Override
    public Boolean getIsOptional(T field) {
        return false;
    }

    @Override
    public Map<T, Boolean> getIsOptionals() {
        return Arrays.stream(getValues())
                .collect(Collectors.toMap(Function.identity(), this::getIsOptional));
    }
}
