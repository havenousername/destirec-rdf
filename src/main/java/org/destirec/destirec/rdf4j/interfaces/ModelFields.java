package org.destirec.destirec.rdf4j.interfaces;

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

    Map<T, Variable> getVariableNames();
    Map<T, IRI> getPredicates();

    Map<T, IRI> getReadPredicates();

    Map<T, CoreDatatype> getTypes();

    IRI getPredicate(T field);
    Variable getVariable(T field);

    CoreDatatype getType(T field);

    String getResourceLocation();
}
