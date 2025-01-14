package org.destirec.destirec.rdf4j.interfaces.daoVisitors;

import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

import java.util.List;

public class SparqlHelperMethods {
    public static Variable createVariable(List<Variable> variables) {
        String variableString = variables
                .stream()
                .map(Variable::getVarName)
                .reduce("", (subtotal, element) ->  subtotal + "_" + element);

        return SparqlBuilder.var(variableString);
    }

    public static Variable createConcatVariable(List<Variable> variables) {
        return SparqlBuilder.var("concat_" + createVariable(variables).getVarName());
    }

    public static Variable createConcatVariable(Variable variable) {
        return SparqlBuilder.var("concat_" + variable.getVarName());
    }
}
