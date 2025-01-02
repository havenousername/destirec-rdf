package org.destirec.destirec.rdf4j.interfaces;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.daoVisitors.GetVariableVisitor;
import org.destirec.destirec.rdf4j.interfaces.daoVisitors.QueryStringVisitor;
import org.destirec.destirec.rdf4j.interfaces.daoVisitors.TriplesVisitor;
import org.destirec.destirec.rdf4j.interfaces.daoVisitors.UpdateBindingsVisitor;
import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            UpdateBindingsVisitor visitor = new UpdateBindingsVisitor(
                    dtoEntity.get(field),
                    valueFactory,
                    bindingsBuilder,
                    modelFields.getType(field)
            );
            variable.accept(visitor);
        });
    }

    @Override
    public NamedSparqlSupplier getInsertSparql(DTO userDto) {
        return NamedSparqlSupplier.of(KEY_PREFIX_INSERT, () -> {
            TriplePattern pattern = modelFields.getId()
                    .isA(migration.get());
            modelFields.getPredicates().forEach((key, value) -> {
                ValueContainer<Variable> variable = modelFields.getVariable(key);
                TriplesVisitor insertVisitor = new TriplesVisitor(pattern, value);
                variable.accept(insertVisitor);
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
        List<Variable> variables = new ArrayList<>(modelFields.getVariableNames().size() + 1);
        variables.add(modelFields.getId());
        modelFields.getVariableNames().values().forEach(variable -> {
            GetVariableVisitor visitor = new GetVariableVisitor();
            variable.accept(visitor);
            variables.addAll(visitor.getVariables());
        });

        TriplePattern pattern = modelFields.getId()
                .isA(migration.get());
        modelFields.getReadPredicates().forEach((key, value) -> {
            TriplesVisitor visitor = new TriplesVisitor(pattern, value);
            modelFields.getVariable(key).accept(visitor);
        });

        System.out.println("GET READ QUERY" + Queries.SELECT(variables.toArray(Variable[]::new))
                .where(pattern)
                .getQueryString() );

        return Queries.SELECT(variables.toArray(Variable[]::new))
                .where(pattern)
                .getQueryString();
    }


    @Override
    protected DTO mapSolution(BindingSet querySolution) {
        IRI id = QueryResultUtils.getIRI(querySolution, modelFields.getId());
        var map = modelFields.getVariableNames()
                .keySet()
                .stream()
                .map((key) -> {
                    var variableContainer = modelFields.getVariable(key);
                    QueryStringVisitor visitor = new QueryStringVisitor(querySolution);
                    variableContainer.accept(visitor);
                    return Map.entry(key, visitor.getQueryString().toString());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.println("MAP SOLUTION");
        return null;
    }

}
