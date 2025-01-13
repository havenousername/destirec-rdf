package org.destirec.destirec.rdf4j.interfaces.daoVisitors;

import org.destirec.destirec.rdf4j.interfaces.ContainerVisitor;
import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;

import java.util.List;

public class TriplesInsertVisitor implements ContainerVisitor<Variable> {
    private final TriplePattern pattern;
    private final ValueContainer<IRI> predicate;

    public TriplesInsertVisitor(TriplePattern pattern, ValueContainer<IRI> predicate) {
        this.pattern = pattern;
        this.predicate = predicate;
    }

    @Override
    public void visit(Variable visitor) {
        pattern.andHas(predicate.next(), visitor);
    }

    @Override
    public void visit(List<Variable> visitor) {
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
