package org.destirec.destirec.rdf4j.interfaces;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.daoVisitors.*;
import org.destirec.destirec.rdf4j.ontology.AppOntology;
import org.destirec.destirec.utils.ValueContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Groupable;
import org.eclipse.rdf4j.sparqlbuilder.core.Projectable;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfResource;
import org.eclipse.rdf4j.spring.dao.SimpleRDF4JCRUDDao;
import org.eclipse.rdf4j.spring.dao.support.bindingsBuilder.MutableBindings;
import org.eclipse.rdf4j.spring.dao.support.sparql.NamedSparqlSupplier;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.eclipse.rdf4j.spring.util.QueryResultUtils;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.destirec.destirec.utils.Constants.MAX_RDF_RECURSION;

@Getter
public abstract class GenericDao<FieldEnum extends Enum<FieldEnum> & ConfigFields.Field, DTO extends Dto> extends SimpleRDF4JCRUDDao<DTO, IRI> {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected final ConfigFields<FieldEnum> configFields;
    protected final Predicate migration;
    protected final DtoCreator<DTO, FieldEnum> dtoCreator;
    protected ValueFactory valueFactory = SimpleValueFactory.getInstance();
    protected AtomicInteger mapEnteredTimes = new AtomicInteger(0);
    protected AppOntology ontology;
    protected ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();



    public GenericDao(
        RDF4JTemplate rdf4JTemplate,
        ConfigFields<FieldEnum> configFields,
        Predicate migration,
        DtoCreator<DTO, FieldEnum> dtoCreator,
        AppOntology ontology
    ) {
        super(rdf4JTemplate);
        this.configFields = configFields;
        this.migration = migration;
        this.dtoCreator = dtoCreator;
        this.ontology = ontology;
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(NamedSparqlSupplierPreparer namedSparqlSupplierPreparer) {
        return null;
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(configFields.getId(), iri);
    }

    @Override
    protected void populateBindingsForUpdate(MutableBindings bindingsBuilder, DTO dto) {
        Map<ConfigFields.Field, String> dtoEntity = dto.getMap();
        configFields.getVariableNames().forEach((field, variable) -> {
            String value = dtoEntity.get(field);
            UpdateBindingsVisitor visitor = new UpdateBindingsVisitor(
                    value,
                    valueFactory,
                    bindingsBuilder,
                    configFields.getType(field),
                    configFields.getIsOptional(field)
            );
            variable.accept(visitor);
        });
    }

    @Override
    public NamedSparqlSupplier getInsertSparql(DTO dto) {
        return NamedSparqlSupplier.of(KEY_PREFIX_INSERT, () -> {
            TriplePattern pattern = configFields.getId()
                    .isA(migration.getResource());
            configFields.getPredicates().forEach((key, value) -> {
                ValueContainer<Variable> variable = configFields.getVariable(key);
                TriplesInsertVisitor insertVisitor = new TriplesInsertVisitor(pattern, value);
                variable.accept(insertVisitor);
            });
            return Queries.INSERT(pattern).getQueryString();
        });
    }


    @Override
    protected IRI getInputId(Dto userDto) {
        if (userDto.id() == null) {
            var userId = getRdf4JTemplate().getNewUUID();
            return SimpleValueFactory.getInstance().createIRI(configFields.getResourceLocation() + userId.stringValue());
        }
        return userDto.id();
    }

    private List<Map.Entry<VariableType, Projectable>> getReadVariables() {
        List<Map.Entry<VariableType, Projectable>> variables = new ArrayList<>(configFields.getVariableNames().size() + 1);
        variables.add(Map.entry(VariableType.SINGULAR, configFields.getId()));
        configFields.getVariableNames().values().forEach(variable -> {
            GetVariableVisitor visitor = new GetVariableVisitor();
            variable.accept(visitor);
            variables.addAll(visitor.getVariables());
        });

        return variables;
    }


    @Override
    protected String getReadQuery() {
        return getReadQuery(migration.getResource());
    }

    protected Triplet<
            List<Map.Entry<VariableType, Projectable>>,
            GraphPattern[],
            Groupable[]
    > getSelectParams(RdfResource graph) {
        mapEnteredTimes.set(0);
        List<Map.Entry<VariableType, Projectable>> variables = getReadVariables();
        List<GraphPattern> graphPatterns = new ArrayList<>();
        graphPatterns.add(configFields.getId().isA(graph));

        configFields.getReadPredicates().forEach((key, value) -> {
            TriplesSelectVisitor visitor = new TriplesSelectVisitor(
                    value,
                    configFields.getId(),
                    configFields.getIsOptional(key),
                    configFields.getFilter(key).orElse(null)
            );
            configFields.getVariable(key).accept(visitor);
            graphPatterns.add(visitor.getPattern());
        });

        var groupByVariables = variables.stream()
                .filter(variable -> variable.getKey() == VariableType.SINGULAR)
                .map(Map.Entry::getValue)
                .map(v -> (Groupable)v)
                .toList();
        var shouldGroupBy = groupByVariables.size() != variables.size();
        Groupable[] groupBy = shouldGroupBy ? groupByVariables.toArray(Groupable[]::new) : new Groupable[0];

        return new Triplet<>(
                variables,
                graphPatterns.toArray(GraphPattern[]::new),
                groupBy
        );
    }


    protected String getReadQuery(RdfResource graph) {
        var queryParams = getSelectParams(graph);
        return Queries.SELECT(queryParams.getValue0()
                        .stream()
                        .map(Map.Entry::getValue)
                        .toArray(Projectable[]::new)
                )
                .distinct()
                .where(queryParams.getValue1())
                .groupBy(queryParams.getValue2())
                .getQueryString();
    }


    @Override
    protected DTO mapSolution(BindingSet querySolution) {
        if (mapEnteredTimes.getAndIncrement() >= MAX_RDF_RECURSION) {
            throw new IllegalCallerException("Reached maximum depth of " + MAX_RDF_RECURSION + ", exiting application.");
        }
        IRI id = QueryResultUtils.getIRI(querySolution, configFields.getId());
        var map = configFields.getVariableNames()
                .keySet()
                .stream()
                .map((key) -> {
                    var variableContainer = configFields.getVariable(key);
                    if (configFields.getIsOptional(key)) {
                        return null;
                    }
                    QueryStringVisitor visitor = new QueryStringVisitor(querySolution);
                    variableContainer.accept(visitor);
                    return Map.entry(key, visitor.getQueryString().toString());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return dtoCreator.create(id, map);
    }

    public List<DTO> listPaginated(int page, int pageSize) {
        String paginatedQuery = getReadQueryPaginated(migration.getResource(), page, pageSize);
        return getRdf4JTemplate()
                .tupleQuery(getClass(), "KEY_READ_PAGINATED_QUERY", () -> paginatedQuery)
                .evaluateAndConvert()
                .toList(this::mapSolution, this::postProcessMappedSolution);
    }

    protected String getReadQueryPaginated(RdfResource graph, int page, int pageSize) {
        var queryParams = getSelectParams(graph);
        return Queries.SELECT(queryParams.getValue0()
                        .stream()
                        .map(Map.Entry::getValue)
                        .toArray(Projectable[]::new))
                .distinct()
                .where(queryParams.getValue1())
                .groupBy(queryParams.getValue2())
                .limit(pageSize)
                .offset(page * pageSize)
                .getQueryString();
    }

    public long getTotalCount() {
        String countQuery = getCountQuery(migration.getResource());
        var countResult = getRdf4JTemplate()
                .tupleQuery(getClass(), "KEY_COUNT_QUERY", () -> countQuery)
                .evaluateAndConvert()
                .toStream()
                .findFirst();

        return countResult.map(bindings -> Long.parseLong(bindings.getBinding("count")
                .getValue().stringValue())).orElse(0L);

    }

    protected String getCountQuery(RdfResource graph) {
        return Queries.SELECT(Expressions.countAll().as(SparqlBuilder.var("count")))
                .where(configFields.getId().isA(graph))
                .getQueryString();
    }


    @Override
    public IRI saveAndReturnId(DTO input) {
        return super.saveAndReturnId(input);
    }

    @Override
    public IRI saveAndReturnId(DTO dto, IRI iri) {
        try {
            return super.saveAndReturnId(dto, iri);
        } catch (Exception exception) {
            logger.error("Cannot save the dto: {} with iri: {}", dto.toString(), iri.toString(), exception);
            throw exception;
        }
    }

    @Override
    public void delete(IRI iri) {
        super.delete(iri);
        ontology.triggerInference();
    }

    @Override
    public RDF4JTemplate getRdf4JTemplate() {
        return super.getRdf4JTemplate();
    }
}
