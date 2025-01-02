package org.destirec.destirec.rdf4j.interfaces;

import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

import java.util.Map;

public interface ModelFields<T extends Enum<T> & ModelFields.Field> {
    interface Field {
        String getName();

        boolean isRead();
    }
    Variable getId();

    Map<T, ValueContainer<Variable>> getVariableNames();
    Map<T, ValueContainer<IRI>> getPredicates();

    Map<T, ValueContainer<IRI>> getReadPredicates();

    Map<T, ValueContainer<CoreDatatype>> getTypes();

    ValueContainer<IRI> getPredicate(T field);

    ValueContainer<Variable> getVariable(T field);

    ValueContainer<CoreDatatype> getType(T field);

    String getResourceLocation();
}
