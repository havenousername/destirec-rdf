package org.destirec.destirec.rdf4j.interfaces.daoVisitors;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.ContainerVisitor;
import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;

import java.util.List;

public class TriplesSelectVisitor implements ContainerVisitor<Variable> {
    private final ValueContainer<IRI> predicate;
    private final Variable id;

    private final boolean isOptional;

    @Getter
    private GraphPattern pattern;

    public TriplesSelectVisitor(ValueContainer<IRI> predicate, Variable id, boolean isOptional) {
        this.predicate = predicate;
        this.id = id;
        this.isOptional = isOptional;
    }

    @Override
    public void visit(Variable visitor) {
        if (!isOptional) {
            pattern = GraphPatterns.tp(id, predicate.next(), visitor);
        } else {
            pattern = GraphPatterns.optional(GraphPatterns.tp(id, predicate.next(), visitor));
        }
    }

    @Override
    public void visit(List<Variable> visitor) {
        Variable variable = SparqlHelperMethods.createVariable(visitor);
        visit(variable);
    }
}
