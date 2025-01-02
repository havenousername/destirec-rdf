package org.destirec.destirec.rdf4j.interfaces.daoVisitors;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.ContainerVisitor;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.spring.util.QueryResultUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class QueryStringVisitor implements ContainerVisitor<Variable> {
    private final BindingSet querySolution;

    @Getter
    private final AtomicReference<String> queryString;

    public QueryStringVisitor(BindingSet querySolution) {
        this.querySolution = querySolution;
        this.queryString = new AtomicReference<>("");
    }

    @Override
    public void visit(Variable visitor) {
        queryString.set(QueryResultUtils.getString(querySolution, visitor));
    }

    @Override
    public void visit(List<Variable> visitor) {
        StringBuilder builder = new StringBuilder();
        visitor.forEach(variable -> {
            String varStr = QueryResultUtils.getString(querySolution, variable);
            builder.append(varStr);
            builder.append(",");
        });
        queryString.set(builder.toString());
    }
}
