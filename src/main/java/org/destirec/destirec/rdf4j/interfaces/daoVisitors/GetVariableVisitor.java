package org.destirec.destirec.rdf4j.interfaces.daoVisitors;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.ContainerVisitor;
import org.destirec.destirec.rdf4j.interfaces.VariableType;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Aggregate;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Projectable;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Getter
public class GetVariableVisitor implements ContainerVisitor<Variable> {
    private final ArrayList<Map.Entry<VariableType, Projectable>> variables;

    public GetVariableVisitor() {
        this.variables = new ArrayList<>();
    }

    @Override
    public void visit(Variable visitor) {
        variables.add(Map.entry(VariableType.SINGULAR, visitor));
    }

    @Override
    public void visit(List<Variable> visitor) {
        Variable variable = SparqlHelperMethods.createVariable(visitor);
        Aggregate expression = Expressions.group_concat("\",\"", variable).distinct();
        Variable groupConcatVariable = SparqlHelperMethods.createConcatVariable(variable);
        variables.add(Map.entry(VariableType.COMPOSITE, SparqlBuilder.as(expression, groupConcatVariable)));
    }
}
