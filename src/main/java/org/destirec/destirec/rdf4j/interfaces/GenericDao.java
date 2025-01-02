package org.destirec.destirec.rdf4j.interfaces;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.container.Container;
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

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
            if (variable.isSingular()) {
                Literal literal = valueFactory.createLiteral(dtoEntity.get(field), modelFields.getType(field).getSingular());
                bindingsBuilder
                        .add(variable.getSingular(), literal);
            } else {
                var variables = variable.getMultiple().stream().toList();
                var types = modelFields.getType(field).getMultiple().stream().toList();

                String[] arrayString = dtoEntity.get(field).split(",");
                for (int i = 0; i < variables.size(); i++) {
                    Literal literal = valueFactory.createLiteral(arrayString[i], types.get(i));
                    bindingsBuilder
                            .add(variables.get(i), literal);
                }
            }

        });
    }

    @Override
    public NamedSparqlSupplier getInsertSparql(DTO userDto) {
        return NamedSparqlSupplier.of(KEY_PREFIX_INSERT, () -> {
            TriplePattern pattern = modelFields.getId()
                    .isA(migration.get());
            modelFields.getPredicates().forEach((key, value) -> {
                Container<Variable> variable = modelFields.getVariable(key);
                if (variable.isSingular() && value.isSingular()) {
                    pattern.andHas(value.getSingular(), variable.getSingular());
                } else {
                    var variables = variable.getMultiple().stream().toList();
                    var values = value.getMultiple().stream().toList();
                    for (int i = 0; i < variables.size(); i++) {
                        pattern.andHas(values.get(i), variables.get(i));
                    }
                }
            });
            System.out.println(Queries.INSERT(pattern).getQueryString());
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
            if (variable.isSingular()) {
                variables[index.getAndIncrement()] = variable.getSingular();
            } else {
                variable.getMultiple().forEach(variableS -> {
                    variables[index.getAndIncrement()] = variableS;
                });
            }
        });

        TriplePattern pattern = modelFields.getId()
                .isA(migration.get());
        modelFields.getReadPredicates().forEach((key, value) -> {
            if (value.isSingular()) {
                pattern.andHas(value.getSingular(), modelFields.getVariable(key).getSingular());
            } else {
                var variablesS = modelFields.getVariable(key).getMultiple().stream().toList();
                var values = value.getMultiple().stream().toList();
                for (int i = 0; i < variablesS.size(); i++) {
                    pattern.andHas(values.get(i), variablesS.get(i));
                }
            }
        });

        return Queries.SELECT(variables)
                .where(pattern)
                .getQueryString();
    }


    @Override
    protected DTO mapSolution(BindingSet querySolution) {
        IRI id = QueryResultUtils.getIRI(querySolution, modelFields.getId());
        var map = modelFields.getVariableNames()
                .keySet().stream()
                .map((key) -> {
                    var variableContainer = modelFields.getVariable(key);
                    AtomicReference<String> queryString = new AtomicReference<>("");
                    if (variableContainer.isSingular()) {
                        queryString.set(QueryResultUtils.getString(querySolution, variableContainer.getSingular()));
                    } else {
                        StringBuilder builder = new StringBuilder();
                        variableContainer.getMultiple().forEach(variable -> {
                            String varStr = QueryResultUtils.getString(querySolution, variable);
                            builder.append(varStr);
                            builder.append(",");
                        });
                        queryString.set(builder.toString());
                    }
                    return Map.entry(key, queryString.toString());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return dtoCreator.create(id, map);
    }

}
