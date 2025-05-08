package org.destirec.destirec.rdf4j.interfaces.daoVisitors;

import org.destirec.destirec.rdf4j.interfaces.ContainerVisitor;
import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
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

    private final boolean isOptional;

    public UpdateBindingsVisitor(
            String dtoValue,
            ValueFactory valueFactory,
            MutableBindings builder,
            ValueContainer<CoreDatatype> coreDatatype,
            boolean isOptional
    ) {
        this.dtoValue = dtoValue;
        this.valueFactory = valueFactory;
        this.builder = builder;
        this.coreDatatype = coreDatatype;
        this.isOptional = isOptional;
    }

    @Override
    public void visit(Variable visitor) {
        if (dtoValue == null && isOptional) {
            return;
        } else if (dtoValue == null) {
            throw new IllegalStateException("Cannot accept empty dtoValue here for the variable " + visitor.getVarName());
        }

        CoreDatatype next;
        if (coreDatatype.hasNext() && (next = coreDatatype.next()) != null) {
            Literal literal = valueFactory.createLiteral(dtoValue, next);
            builder
                    .add(visitor, literal);
        } else {
            builder.add(visitor, dtoValue);
        }

    }

    @Override
    public void visit(List<Variable> visitor) {
        if (dtoValue == null) {
            return;
        }
        String[] arrayString = dtoValue.split(",");
        CoreDatatype datatype = coreDatatype.hasNext() ? coreDatatype.next() : null;
        for (int i = 0; i < arrayString.length && i < visitor.size(); i++) {
            if (coreDatatype.hasNext()) {
                datatype = coreDatatype.next();
            }
            if (datatype != null) {
                Literal literal = valueFactory.createLiteral(arrayString[i], datatype);
                builder
                        .add(visitor.get(i), literal);
            } else {
                IRI iri = valueFactory.createIRI(arrayString[i]);
                builder.add(visitor.get(i), iri);
            }

        }
    }
}
