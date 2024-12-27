package org.destirec.destirec.rdf4j.model.resource;

import lombok.Getter;
import lombok.NonNull;
import org.destirec.destirec.rdf4j.model.predicates.DomainPredicate;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.util.ModelBuilder;

import java.util.ArrayList;
import java.util.Map;

@Getter
public abstract class NestedResourceRDF<T> extends Resource<T> {
    private DomainPredicate nestedPredicate;
    public NestedResourceRDF(String name, Map<T, IRI> fields, Map<T, CoreDatatype> fieldTypes) {
        super(name, fields, fieldTypes);
        nestedPredicate = new DomainPredicate("nestedPredicate", get(), get());
    }

    public void setNestedPredicate(Map.Entry<IRI, String> nestedPredicate) {
        this.nestedPredicate = new DomainPredicate(
                nestedPredicate.getValue(),
                nestedPredicate.getKey(),
                get()
        );
    }


    public void addPropertiesToResource(ModelBuilder builder, String graphName, @NonNull IRI resource,  ArrayList<Map<T, String>> values) {
        builder
                .namedGraph(graphName)
                .subject(resource);
        values.forEach(value -> {
            builder.subject(resource);

            BNode bNode = valueFactory.createBNode();
            builder.add(nestedPredicate.get(), bNode);

            builder.subject(bNode);

            generateResourceFields(builder, value);
        });
    }
}
