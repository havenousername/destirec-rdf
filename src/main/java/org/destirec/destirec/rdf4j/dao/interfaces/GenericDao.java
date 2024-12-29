package org.destirec.destirec.rdf4j.dao.interfaces;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.spring.dao.SimpleRDF4JCRUDDao;
import org.eclipse.rdf4j.spring.dao.support.bindingsBuilder.MutableBindings;
import org.eclipse.rdf4j.spring.dao.support.sparql.NamedSparqlSupplier;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GenericDao<FieldEnum extends Enum<FieldEnum> & ModelFields.Field, DTO extends Dto> extends SimpleRDF4JCRUDDao<DTO, IRI> {
    private final ModelFields<FieldEnum> modelFields;
    private final Predicate migration;


    public GenericDao(
        RDF4JTemplate rdf4JTemplate,
        ModelFields<FieldEnum> modelFields,
        Predicate migration
    ) {
        super(rdf4JTemplate);
        this.modelFields = modelFields;
        this.migration = migration;
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(NamedSparqlSupplierPreparer namedSparqlSupplierPreparer) {
        return null;
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(modelFields.getId(), iri);
    }

    @Override
    protected void populateBindingsForUpdate(MutableBindings bindingsBuilder, DTO userDto) {
        List<String> userDtoList = userDto.getList();
        AtomicInteger index = new AtomicInteger();
        modelFields.getVariableNames().forEach((field, variable) -> {
            bindingsBuilder
                    .add(variable, userDtoList.get(index.get()));
            index.getAndIncrement();
        });
    }

    @Override
    protected NamedSparqlSupplier getInsertSparql(DTO userDto) {
        return NamedSparqlSupplier.of(KEY_PREFIX_INSERT, () -> {
            TriplePattern pattern = modelFields.getId()
                    .isA(migration.get());
            modelFields.getPredicates().forEach((key, value) ->
                    pattern.andHas(value, modelFields.getVariable(key)));
            return Queries.INSERT(pattern).getQueryString();
        });
    }


    @Override
    protected IRI getInputId(Dto userDto) {
        if (userDto.id() == null) {
            var userId = getRdf4JTemplate().getNewUUID();
            return SimpleValueFactory.getInstance().createIRI(modelFields.getResourceLocation() + userId.stringValue());
        }
        return userDto.id();
    }


    @Override
    protected String getReadQuery() {
        Variable[] variables = new Variable[modelFields.getVariableNames().size() + 1];

        AtomicInteger index = new AtomicInteger();
        variables[index.getAndIncrement()] = modelFields.getId();
        modelFields.getVariableNames().values().forEach(variable -> {
            variables[index.getAndIncrement()] = variable;
        });

        TriplePattern pattern = modelFields.getId()
                .isA(migration.get());
        modelFields.getReadPredicates().forEach((key, value) ->
                pattern.andHas(value, modelFields.getVariable(key)));

        return Queries.SELECT(variables)
                .where(pattern)
                .getQueryString();
    }

}
