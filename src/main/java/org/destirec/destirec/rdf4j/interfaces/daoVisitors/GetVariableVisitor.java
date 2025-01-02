package org.destirec.destirec.rdf4j.interfaces.daoVisitors;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.ContainerVisitor;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GetVariableVisitor implements ContainerVisitor<Variable> {
    private final ArrayList<Variable> variables;

    public GetVariableVisitor() {
        this.variables = new ArrayList<>();
    }

    @Override
    public void visit(Variable visitor) {
        variables.add(visitor);
    }

    @Override
    public void visit(List<Variable> visitor) {
        variables.addAll(visitor);
    }
}
