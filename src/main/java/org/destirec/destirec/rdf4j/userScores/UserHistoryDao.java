package org.destirec.destirec.rdf4j.userScores;

import lombok.Getter;
import org.destirec.destirec.rdf4j.Rdf4JService;
import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.interfaces.Rdf4jTemplate;
import org.destirec.destirec.rdf4j.ontology.AppOntology;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.SimpleDtoTransformations;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.destirec.destirec.utils.rdfDictionary.UserNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.InsertDataQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.spring.dao.support.sparql.NamedSparqlSupplier;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Getter
@Repository
public class UserHistoryDao extends GenericDao<UserHistoryConfig.Fields, UserHistoryDto> {
    private final Rdf4JService rdf4JService;
    private final Rdf4jTemplate rdf4jTemplate;
    private static final String INFLUENCE_RESOURCE = DESTIREC.wrapNamespace( "influence/", DESTIREC.UriType.RESOURCE);

    public UserHistoryDao(
            RDF4JTemplate rdf4JTemplate,
            UserHistoryConfig configFields,
            UserHistoryMigration migration,
            UserHistoryDtoCreator dtoCreator,
            AppOntology ontology, Rdf4JService rdf4JService, Rdf4jTemplate rdf4jTemplate) {
        super(rdf4JTemplate, configFields, migration, dtoCreator, ontology);
        this.rdf4JService = rdf4JService;
        this.rdf4jTemplate = rdf4jTemplate;
    }


    public Optional<UserInfluenceDto> getInfluence(IRI region, IRI user) {
        return this.getRdf4JTemplate()
                .tupleQuery(getClass(), "KEY_GET_INFLUENCE", () ->
                        getInfluenceQuery(region, user))
                .evaluateAndConvert()
                .toSingletonOptional(solution -> {
                    IRI regionSolution = (IRI) solution.getValue("region");
                    IRI userSolution = (IRI) solution.getValue("user");
                    IRI id = (IRI) solution.getValue("historyInfluence");

                    List<String> rawScores = SimpleDtoTransformations.
                            toListString(solution.getValue("scores").stringValue());

                    List<String> rawConfidences = SimpleDtoTransformations.
                            toListString(solution.getValue("confidences").stringValue());

                    List<Double> scores = rawScores.stream().map(Double::parseDouble).toList();
                    List<Double> confidences = rawConfidences.stream().map(Double::parseDouble).toList();

                    return new UserInfluenceDto(id, regionSolution, userSolution, scores, confidences);
                });
    }

    public IRI updateInfluence(UserInfluenceDto influenceDto) {
        this.getRdf4JTemplate().update(this.getClass(), createInfluenceSupplier(influenceDto.getId(), influenceDto))
                .execute();

        return influenceDto.getId();
    }


    public IRI createInfluence(UserInfluenceDto influenceDto) {
        IRI newInfluence = valueFactory.createIRI(INFLUENCE_RESOURCE + "region:" + influenceDto.getRegion()  +"-user:" + influenceDto.getUser());
        this.getRdf4JTemplate().update(this.getClass(), createInfluenceSupplier(newInfluence, influenceDto))
                .execute();

        return newInfluence;
    }

    public NamedSparqlSupplier createInfluenceSupplier(IRI id, UserInfluenceDto dto) {
        return NamedSparqlSupplier.of(KEY_PREFIX_INSERT, () -> createInfluenceQuery(id, dto));
    }


    public boolean hasInfluence(IRI region, IRI user) {
        Variable influenceVar = SparqlBuilder.var("influence");
        TriplePattern typeStatement = GraphPatterns.tp(
                influenceVar,
                RDF.TYPE,
                TopOntologyNames.Classes.VERSION.rdfIri()
        );

        TriplePattern hasRegion = GraphPatterns.tp(
                influenceVar,
                UserNames.Properties.INFLUENCE_FOR_REGION.rdfIri(),
                region
        );

        TriplePattern hasUser = GraphPatterns.tp(
                influenceVar,
                UserNames.Properties.INFLUENCE_BY_USER.rdfIri(),
                user
        );


        GraphPattern queryPattern = GraphPatterns.and(typeStatement, hasRegion, hasUser);

        String queryAsk = "ASK " + queryPattern;
        BooleanQuery askQuery = rdf4jTemplate
                .applyToConnection(connection -> connection.prepareBooleanQuery(QueryLanguage.SPARQL, queryAsk));
        return askQuery.evaluate();
    }


    private String getInfluenceQuery(IRI region, IRI user) {
        Variable historyInfluenceVar = SparqlBuilder.var("historyInfluence");
        Variable regionVar = SparqlBuilder.var("region");
        Variable userVar = SparqlBuilder.var("user");

        Variable scores = SparqlBuilder.var("scores");
        Variable confidences = SparqlBuilder.var("confidences");

        TriplePattern isHistoryInfluence = GraphPatterns
                .tp(historyInfluenceVar, RDF.TYPE, UserNames.Classes.USER_HISTORY_INFLUENCE.rdfIri());
        GraphPattern valuesRegion = GraphPatterns
                .and().values(variablesBuilder -> {
                    variablesBuilder.variables(regionVar);
                    variablesBuilder.value(Rdf.iri(region));
                });

        GraphPattern valuesUser = GraphPatterns
                .and().values(variablesBuilder -> {
                    variablesBuilder.variables(userVar);
                    variablesBuilder.value(Rdf.iri(user));
                });

        TriplePattern hasUser = GraphPatterns
                .tp(historyInfluenceVar, UserNames.Properties.INFLUENCE_FOR_REGION.rdfIri(), userVar);

        TriplePattern hasRegion = GraphPatterns
                .tp(historyInfluenceVar, UserNames.Properties.INFLUENCE_BY_USER.rdfIri(), regionVar);

        TriplePattern hasCConfidences = GraphPatterns
                .tp(historyInfluenceVar, UserNames.Properties.HAS_C_CONFIDENCES.rdfIri(), confidences);

        TriplePattern hasPScores = GraphPatterns
                .tp(historyInfluenceVar, UserNames.Properties.HAS_P_SCORES.rdfIri(), scores);

        String selectQuery = Queries.SELECT(historyInfluenceVar, confidences, scores)
                .where(
                        isHistoryInfluence,
                        valuesRegion,
                        valuesUser,
                        hasUser,
                        hasRegion,
                        hasCConfidences,
                        hasPScores
                )
                .toString();

        return selectQuery;
    }

    private String createListToStringFormatted(List<Double> scores) {
        return String.join(",", scores.stream().map(String::valueOf).toArray(CharSequence[]::new));
    }


    public String createInfluenceQuery(IRI id, UserInfluenceDto dto) {
        TriplePattern typeStatement = GraphPatterns.tp(
                id,
                RDF.TYPE,
                UserNames.Classes.USER_HISTORY_INFLUENCE.rdfIri()
        );
        TriplePattern hasRegionProperty = GraphPatterns.tp(
                id,
                UserNames.Properties.INFLUENCE_FOR_REGION.rdfIri(), // Property linking to a region
                dto.getRegion() // The region IRI
        );

        TriplePattern hasUserProperty = GraphPatterns.tp(
                id,
                UserNames.Properties.INFLUENCE_BY_USER.rdfIri(),   // Property linking to a user
                dto.getUser()   // The user IRI
        );

        Literal scoreLiteral = valueFactory.createLiteral(createListToStringFormatted(dto.getScores()));
        Literal confidenceLiteral = valueFactory.createLiteral(createListToStringFormatted(dto.getConfidences()));

        TriplePattern hasPScore = GraphPatterns.tp(
                id,
                UserNames.Properties.HAS_INFLUENCE_P_SCORE.rdfIri(),
                scoreLiteral // or scoreRdfLiteral
        );

        TriplePattern hasCConfidence = GraphPatterns.tp(
                id,
                UserNames.Properties.HAS_INFLUENCE_C_CONFIDENCE.rdfIri(),
                confidenceLiteral // or confidenceRdfLiteral
        );

        InsertDataQuery insertDataQuery = Queries.INSERT_DATA(
                typeStatement,
                hasRegionProperty,
                hasUserProperty,
                hasPScore,
                hasCConfidence
        );

        return insertDataQuery.getQueryString();
    }
}
