package org.destirec.destirec.rdf4j.interfaces;

import lombok.Getter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.spring.dao.SimpleRDF4JCRUDDao;
import org.eclipse.rdf4j.spring.dao.support.bindingsBuilder.MutableBindings;
import org.eclipse.rdf4j.spring.dao.support.sparql.NamedSparqlSupplier;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.eclipse.rdf4j.spring.util.QueryResultUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
public abstract class GenericDao<FieldEnum extends Enum<FieldEnum> & ModelFields.Field, DTO extends Dto> extends SimpleRDF4JCRUDDao<DTO, IRI> {
    protected final ModelFields<FieldEnum> modelFields;
    protected final Predicate migration;
    protected final DtoCreator<DTO, FieldEnum> dtoCreator;
    protected ValueFactory valueFactory = SimpleValueFactory.getInstance();


    public GenericDao(
        RDF4JTemplate rdf4JTemplate,
        ModelFields<FieldEnum> modelFields,
        Predicate migration,
        DtoCreator<DTO, FieldEnum> dtoCreator
    ) {
        super(rdf4JTemplate);
        this.modelFields = modelFields;
        this.migration = migration;
        this.dtoCreator = dtoCreator;
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
    protected void populateBindingsForUpdate(MutableBindings bindingsBuilder, DTO dto) {
        Map<ModelFields.Field, String> dtoEntity = dto.getMap();
        modelFields.getVariableNames().forEach((field, variable) -> {
            Literal literal = valueFactory.createLiteral(dtoEntity.get(field), modelFields.getType(field));
            bindingsBuilder
                    .add(variable, literal);
        });
    }

    @Override
    public NamedSparqlSupplier getInsertSparql(DTO userDto) {
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


    @Override
    protected DTO mapSolution(BindingSet querySolution) {
        IRI id = QueryResultUtils.getIRI(querySolution, modelFields.getId());
        var map = modelFields.getVariableNames()
                .keySet().stream()
                .map((key) -> Map.entry(key,
                        QueryResultUtils.getString(querySolution, modelFields.getVariable(key))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return dtoCreator.create(id, map);
    }

}
