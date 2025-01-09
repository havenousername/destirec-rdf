package org.destirec.destirec.rdf4j.interfaces.daoVisitors;

import org.destirec.destirec.rdf4j.interfaces.ContainerVisitor;
import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;

import java.util.List;

public class TriplesVisitor implements ContainerVisitor<Variable> {
    private final TriplePattern pattern;
    private final ValueContainer<IRI> predicate;

    private final boolean combinable;

    public TriplesVisitor(TriplePattern pattern, ValueContainer<IRI> predicate, boolean combinable) {
        this.pattern = pattern;
        this.predicate = predicate;
        this.combinable = combinable;
    }

    @Override
    public void visit(Variable visitor) {
        pattern.andHas(predicate.next(), visitor);
    }

    @Override
    public void visit(List<Variable> visitor) {
        if (combinable) {
            Variable variable = SparqlHelperMethods.createVariable(visitor);
            pattern.andHas(predicate.next(), variable);
            return;
        }
        IRI iriInstance = predicate.next();
        if (predicate.hasNext()) {
            for (Variable variable : visitor) {
                if (predicate.hasNext()) {
                    iriInstance = predicate.next();
                    pattern.andHas(iriInstance, variable);
                }
                pattern.andHas(iriInstance, variable);
            }
        } else {
            pattern.andHas(iriInstance, visitor.toArray(Variable[]::new));
        }
    }
}
