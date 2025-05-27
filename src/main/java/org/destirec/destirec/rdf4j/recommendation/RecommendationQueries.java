package org.destirec.destirec.rdf4j.recommendation;

import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.RecommendationNames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.UserNames;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Bind;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.constraint.SparqlFunction;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ConstructQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.*;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfLiteral;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;
import org.springframework.stereotype.Repository;

@Repository
public class RecommendationQueries {
    private final Variable region = SparqlBuilder.var("region");
    private final Variable user = SparqlBuilder.var("user");
    private final Variable totalPreferences = SparqlBuilder.var("totalPreferences");
    private final Variable matchingPreferences = SparqlBuilder.var("matchingPreferences");

    private final Variable preference = SparqlBuilder.var("preference");
    private final Variable hasFQuality = SparqlBuilder.var("hasFQuality");
    private final SimpleValueFactory vf = SimpleValueFactory.getInstance();
    private final Variable featureRegion = SparqlBuilder.var("featureRegion");
    private final Variable scorePreference = SparqlBuilder.var("scorePreference");
    private final Variable scoreRegion = SparqlBuilder.var("scoreRegion");

    private final Variable aggregateOfFeatures = SparqlBuilder.var("scoreRegion");
    private final Variable avgDeltaScore = SparqlBuilder.var("avgDeltaScore");

    public static String WORDS_SEPARATOR = "; ";



    private SubSelect getUserQualitiesQuery() {
        Variable quality = SparqlBuilder.var("quality");

        return GraphPatterns.select(user, Expressions.countAll().as(totalPreferences))
                .where(
                        GraphPatterns.tp(preference, RDF.TYPE, UserNames.Classes.USER_WITH_PREFERENCE.rdfIri()),
                        GraphPatterns.tp(preference, DC.CREATOR, user),
                        GraphPatterns.tp(preference, hasFQuality, quality),
                        GraphPatterns.tp(hasFQuality, RDFS.SUBPROPERTYOF, AttributeNames.Properties.HAS_QUALITY.rdfIri()))
                .groupBy(user);
    }

    private SubSelect getExactRegionUserQualitiesQuery() {
        Variable quality = SparqlBuilder.var("quality");
        return GraphPatterns.select(user, region, Expressions.count(hasFQuality).distinct().as(totalPreferences))
                .where(
                        GraphPatterns.tp(preference, RDF.TYPE, UserNames.Classes.USER_WITH_PREFERENCE.rdfIri()),
                        GraphPatterns.tp(preference, DC.CREATOR, user),
                        GraphPatterns.tp(preference, hasFQuality, quality),
                        GraphPatterns.tp(hasFQuality, RDFS.SUBPROPERTYOF, AttributeNames.Properties.HAS_QUALITY.rdfIri()),
                        GraphPatterns.tp(region, RDF.TYPE, RegionNames.Classes.LEAF_REGION.rdfIri()),
                        GraphPatterns.tp(region, hasFQuality, quality)
                )
                .groupBy(user, region);
    }


    private SubSelect getBiggerThanUserQualitiesQuery(RecommendationParameters parameters) {
        Variable qualityRegion = SparqlBuilder.var("qualityRegion");
        // region which contains the origin of the feature
        Variable someRegion = SparqlBuilder.var("someRegion");

        Variable qualityPreference = SparqlBuilder.var("qualityPreference");
        Variable featurePreference = SparqlBuilder.var("featurePreference");

        // hierarchy variables
        Variable fromParent = SparqlBuilder.var("fromParent");
        Iri fromParentValue = Rdf.iri(parameters.getFromRegion());

        Variable fromRegionType = SparqlBuilder.var("fromRegionType");
        Iri fromRegionValue = Rdf.iri(parameters.getFromRegionType().iri().rdfIri());
        Variable toRegionType = SparqlBuilder.var("toRegionType");
        Iri toRegionValue = Rdf.iri(parameters.getToRegionType().iri().rdfIri());

        // parameters
        Variable tolerance = SparqlBuilder.var("tolerance");
        RdfLiteral<?> toleranceLiteral = Rdf.literalOf(parameters.getTolerance());

        // ^hasFeature
        RdfPredicate reverseHasFeatureIRI = () -> "^<" + AttributeNames.Properties.HAS_FEATURE.pseudoUri() + ">";

        Expression<?> replaceExpr = Expressions.function(
                SparqlFunction.REPLACE,
                Expressions.str(hasFQuality),
                Rdf.literalOf("^.*/"),
                Rdf.literalOf("")
        );

        Variable deltaScore = SparqlBuilder.var("deltaScore");
        Bind deltaScoreBind = Expressions.bind(Expressions.subtract(scoreRegion, scorePreference), deltaScore);
        GraphPatternNotTriples bindScoreTriple = GraphPatterns.and(deltaScoreBind);

        return GraphPatterns.select(user, region, Expressions.count(hasFQuality).distinct().as(matchingPreferences),
                        Expressions.group_concat("\"%s\"".formatted(WORDS_SEPARATOR), replaceExpr).distinct().as(aggregateOfFeatures),
                        Expressions.divide(Expressions.sum(deltaScore), Expressions.avg(deltaScore)).as(avgDeltaScore))
                .where(
                        // Class bindings
                        GraphPatterns.and().values(builder -> {
                           builder.variables(fromRegionType);
                           builder.value(fromRegionValue);
                        }),
                        GraphPatterns.and().values(builder -> {
                            builder.variables(toRegionType);
                            builder.value(toRegionValue);
                        }),
                        GraphPatterns.and().values(builder -> {
                            builder.variables(fromParent);
                            builder.value(fromParentValue);
                        }),
                        // REGION triples
                        GraphPatterns.tp(region, RDF.TYPE, RegionNames.Classes.REGION.rdfIri()),
                        GraphPatterns.tp(region, RegionNames.Properties.HAS_LEVEL.rdfIri(), toRegionType),
                        GraphPatterns.tp(region, vf.createIRI(RegionNames.Properties.SF_WITHIN), fromParent),
                        GraphPatterns.tp(fromParent, RegionNames.Properties.HAS_LEVEL.rdfIri(), fromRegionType),
                        GraphPatterns.tp(region, hasFQuality, qualityRegion),
                        GraphPatterns.tp(hasFQuality, RDFS.SUBPROPERTYOF, AttributeNames.Properties.HAS_QUALITY.rdfIri()),
                        GraphPatterns.tp(hasFQuality, RegionNames.Properties.FOR_FEATURE.rdfIri(), featureRegion),
                        GraphPatterns.tp(featureRegion, AttributeNames.Properties.HAS_SCORE.rdfIri() ,scoreRegion),
                        GraphPatterns.tp(featureRegion, reverseHasFeatureIRI, someRegion),
                        GraphPatterns.tp(someRegion, vf.createIRI(RegionNames.Properties.SF_WITHIN), region),
                        // USER_PREFERENCE triples
                        GraphPatterns.tp(preference, RDF.TYPE, UserNames.Classes.USER_WITH_PREFERENCE.rdfIri()),
                        GraphPatterns.tp(preference, DC.CREATOR, user),
                        GraphPatterns.tp(preference, hasFQuality, qualityPreference),
                        GraphPatterns.tp(hasFQuality, RegionNames.Properties.FOR_FEATURE.rdfIri(), featurePreference),
                        GraphPatterns.tp(featurePreference, AttributeNames.Properties.HAS_SCORE.rdfIri(), scorePreference),
                        GraphPatterns.tp(featurePreference, reverseHasFeatureIRI, preference),
                        GraphPatterns.and().values(builder -> {
                            builder.variables(tolerance);
                            builder.value(toleranceLiteral);
                        }),
                        bindScoreTriple,
                        GraphPatterns.and().filter(Expressions.gt(scoreRegion, Expressions.add(scorePreference, tolerance)))
                )
                .groupBy(user, region);
    }

    public String simpleRecommendationQuery() {
        //   ?region a :SimpleRecommendation
        TriplePattern regionIsRecommendation = GraphPatterns
                .tp(region, RDF.TYPE, RecommendationNames.Classes.SIMPLE_RECOMMENDATION.rdfIri());

        //  ?region :recommendedFor ?user
        TriplePattern recommendedFor = GraphPatterns
                .tp(region, RecommendationNames.Properties.RECOMMENDED_FOR.rdfIri(), user);

        //    SELECT ?user (COUNT(*) AS ?totalPrefs)
        SubSelect userQualitiesQuery = getUserQualitiesQuery();
        SubSelect regionQualitiesQuery = getExactRegionUserQualitiesQuery();

        GraphPatternNotTriples filter = GraphPatterns.and()
                .filter(Expressions.equals(totalPreferences, matchingPreferences));


        ConstructQuery constructQuery = Queries.CONSTRUCT(regionIsRecommendation, recommendedFor)
                .where(userQualitiesQuery, regionQualitiesQuery, filter);

        return constructQuery.getQueryString();
    }

    public String biggerThanRecommendationQuery(RecommendationParameters parameters) {
        TriplePattern regionIsRecommendation = GraphPatterns
                .tp(region, RDF.TYPE, RecommendationNames.Classes.BIGGER_THAN_RECOMMENDATION.rdfIri());

        TriplePattern recommendedFor = GraphPatterns
                .tp(region, RecommendationNames.Properties.RECOMMENDED_FOR.rdfIri(), user);

        SubSelect userQualitiesQuery = getUserQualitiesQuery();
        SubSelect regionQualitiesQuery = getBiggerThanUserQualitiesQuery(parameters);

        Variable minMatchRatio = SparqlBuilder.var("minMatchRatio");
        GraphPattern addMatchRatioToGraph = GraphPatterns.and().values(builder -> {
            builder.variables(minMatchRatio);
            builder.value(Rdf.literalOf(parameters.getMatchRatio()));
        });

        Variable matchRatio = SparqlBuilder.var("matchRatio");
        Bind matchRatioBind = Expressions.bind(Expressions.divide(matchingPreferences, totalPreferences), matchRatio);
        GraphPatternNotTriples filter = GraphPatterns
                .and(matchRatioBind)
                .filter(Expressions.gt(matchRatio, minMatchRatio));

        TriplePattern confidenceLevel = GraphPatterns.tp(
                region, RecommendationNames.Properties.CONFIDENCE_LEVEL.rdfIri(), matchRatio
        );


        String queryString;
        if (parameters.isAddExplanations()) {
            Variable explanation = SparqlBuilder.var("explanation");
            GraphPattern addBNodeExplanation = GraphPatterns.and(Expressions.bind(Expressions.bnode(), explanation));

            TriplePattern hasExplanation = GraphPatterns.tp(region,  RecommendationNames.Properties.HAS_EXPLANATION.rdfIri(), explanation);
            TriplePattern explanationType = GraphPatterns.tp(explanation,  RDF.TYPE, RecommendationNames.Classes.EXPLANATION.rdfIri());
            TriplePattern explanationFeature = GraphPatterns.tp(
                    explanation,
                    RecommendationNames.Properties.EXPLAINS_FEATURE.rdfIri(),
                    aggregateOfFeatures);
            TriplePattern explanationDeltaScore = GraphPatterns.tp(
                    explanation,
                    RecommendationNames.Properties.EXCEEDS_BY_SCORE.rdfIri(),
                    avgDeltaScore
            );

            ConstructQuery constructQuery = Queries.CONSTRUCT(
                            regionIsRecommendation,
                            recommendedFor,
                            confidenceLevel,
                            hasExplanation,
                            explanationType,
                            explanationFeature,
                            explanationDeltaScore
                    )
                    .where(
                            userQualitiesQuery,
                            regionQualitiesQuery,
                            addBNodeExplanation,
                            addMatchRatioToGraph,
                            filter)
                    .orderBy(SparqlBuilder.desc(matchRatio))
                    .limit(parameters.getMaxResults());
            queryString = constructQuery.getQueryString();
        } else {
            ConstructQuery constructQuery = Queries.CONSTRUCT(
                            regionIsRecommendation,
                            recommendedFor,
                            confidenceLevel
                    )
                    .where(userQualitiesQuery, regionQualitiesQuery, addMatchRatioToGraph, filter)
                    .orderBy(SparqlBuilder.desc(matchRatio))
                    .limit(parameters.getMaxResults());
            queryString = constructQuery.getQueryString();
        }

        return queryString;
    }
}
