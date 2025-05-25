package org.destirec.destirec.rdf4j.interfaces;

import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

import java.util.Map;
import java.util.Optional;

public interface ConfigFields<T extends Enum<T> & ConfigFields.Field> {
    interface Field {
        String getName();

        boolean isRead();
    }
    Variable getId();

    Map<T, ValueContainer<Variable>> getVariableNames();
    Map<T, ValueContainer<IRI>> getPredicates();

    Map<T, Boolean> getIsOptionals();

    Map<T, ValueContainer<IRI>> getReadPredicates();

    Map<T, ValueContainer<CoreDatatype>> getTypes();

    ValueContainer<IRI> getPredicate(T field);

    ValueContainer<Variable> getVariable(T field);

    ValueContainer<CoreDatatype> getType(T field);

    Boolean getIsOptional(T field);
    Optional<Expression<?>> getFilter(T field);

    String getResourceLocation();
}
