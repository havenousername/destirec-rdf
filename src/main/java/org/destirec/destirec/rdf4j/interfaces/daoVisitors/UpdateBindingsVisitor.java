package org.destirec.destirec.rdf4j.interfaces.daoVisitors;

import org.destirec.destirec.rdf4j.interfaces.ContainerVisitor;
import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.spring.dao.support.bindingsBuilder.MutableBindings;

import java.util.List;

public class UpdateBindingsVisitor implements ContainerVisitor<Variable> {
    private final String dtoValue;
    private final ValueFactory valueFactory;
    private final MutableBindings builder;

    private final ValueContainer<CoreDatatype> coreDatatype;

    public UpdateBindingsVisitor(String dtoValue, ValueFactory valueFactory, MutableBindings builder, ValueContainer<CoreDatatype> coreDatatype) {
        this.dtoValue = dtoValue;
        this.valueFactory = valueFactory;
        this.builder = builder;
        this.coreDatatype = coreDatatype;
    }

    @Override
    public void visit(Variable visitor) {
        Literal literal = valueFactory.createLiteral(dtoValue, coreDatatype.next());
        builder
                .add(visitor, literal);
    }

    @Override
    public void visit(List<Variable> visitor) {
        String[] arrayString = dtoValue.split(",");
        CoreDatatype datatype = coreDatatype.next();
        for (int i = 0; i < visitor.size(); i++) {
            if (coreDatatype.hasNext()) {
                datatype = coreDatatype.next();
            }
            Literal literal = valueFactory.createLiteral(arrayString[i], datatype);
            builder
                    .add(visitor.get(i), literal);
        }
    }
}
