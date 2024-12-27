package org.destirec.destirec.rdf4j.model.resource;

import lombok.Getter;
import lombok.NonNull;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Resource<T> implements ResourceRDF<T> {
    protected final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Getter
    protected final IRI iri;

    @Getter
    protected final Map<T, IRI> fields;

    @Getter
    protected final Map<T, CoreDatatype> fieldTypes;

    @Getter
    protected AtomicInteger increment;

    @Getter
    protected IRI lastResource;

    public Resource(String name, Map<T, IRI> fields, Map<T, CoreDatatype> fieldTypes) {
        iri = valueFactory.createIRI(DESTIREC.NAMESPACE, name);
        this.fields = fields;
        this.fieldTypes = fieldTypes;
    }

    @Override
    public IRI get() {
        return iri;
    }

    protected void generateResourceFields(ModelBuilder builder, Map<T, String> values) {
        for (T key : values.keySet()) {
            Literal literal = valueFactory.createLiteral(values.get(key), fieldTypes.get(key));
            builder.add(fields.get(key), literal);
        }
    }

    @Override
    public void addPropertiesToResource(ModelBuilder builder, String graphName, @NonNull IRI resource, Map<T, String> values) {
        builder
                .namedGraph(graphName)
                .subject(resource);
        generateResourceFields(builder, values);
    }

    @Override
    public void createResource(ModelBuilder builder, String graphName) {
        lastResource = valueFactory
                .createIRI(DESTIREC.NAMESPACE,  getResourceLocation() + getIncrement());

        builder
                .namedGraph(graphName)
                .subject(lastResource);
        builder.add(RDF.TYPE, get());
    }
}
