package org.destirec.destirec.rdf4j.interfaces;

import org.destirec.destirec.rdf4j.interfaces.container.Container;
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

    Map<T, Container<Variable>> getVariableNames();
    Map<T, Container<IRI>> getPredicates();

    Map<T, Container<IRI>> getReadPredicates();

    Map<T, Container<CoreDatatype>> getTypes();

    Container<IRI> getPredicate(T field);

    Container<Variable> getVariable(T field);

    Container<CoreDatatype> getType(T field);

    String getResourceLocation();
}
