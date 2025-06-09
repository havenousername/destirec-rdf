package org.destirec.destirec.rdf4j.recommendation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.RecommendationNames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.UserNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.CoreDatatype;
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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



    private SubSelect getUserQualitiesQuery(IRI userId) {
        Variable quality = SparqlBuilder.var("quality");

        var bindUserIri = GraphPatterns.and().values(builder -> {
           builder.variables(user);
           builder.value(Rdf.iri(userId));
        });
        return GraphPatterns.select(user, Expressions.countAll().as(totalPreferences))
                .where(
                        bindUserIri,
                        GraphPatterns.tp(preference, RDF.TYPE, UserNames.Classes.USER_WITH_PREFERENCE.rdfIri()),
                        GraphPatterns.tp(preference, DC.CREATOR, user),
                        GraphPatterns.tp(preference, hasFQuality, quality),
                        GraphPatterns.tp(hasFQuality, RDFS.SUBPROPERTYOF, AttributeNames.Properties.HAS_QUALITY.rdfIri()))
                .groupBy(user);
    }

    private SubSelect getUserQualitiesQueryUpdated(IRI userId) {
        Variable quality = SparqlBuilder.var("quality");
        Variable featureName = SparqlBuilder.var("featureName");

        var bindUserIri = GraphPatterns.and().values(builder -> {
            builder.variables(user);
            builder.value(Rdf.iri(userId));
        });
        return GraphPatterns.select(user, Expressions.count(featureName).distinct().as(totalPreferences))
                .where(
                        bindUserIri,
                        GraphPatterns.tp(preference, RDF.TYPE, UserNames.Classes.USER_WITH_PREFERENCE.rdfIri()),
                        GraphPatterns.tp(preference, DC.CREATOR, user),
                        GraphPatterns.tp(preference, hasFQuality, quality),
                        GraphPatterns.tp(hasFQuality, RDFS.SUBPROPERTYOF, AttributeNames.Properties.HAS_QUALITY.rdfIri()),
                        GraphPatterns.tp(hasFQuality, RegionNames.Properties.FOR_FEATURE.rdfIri(), featureRegion),
                        GraphPatterns.tp(featureRegion, AttributeNames.Properties.HAS_REGION_FEATURE.rdfIri(), featureName)
                )
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

    @Getter
    @AllArgsConstructor
    private enum PreferenceWeight {
        VERY_VERY_IMPORTANT(90, 1000),
        VERY_IMPORTANT(80, 500),
        IMPORTANT(70, 250),
        MORE_IMPORTANT(60, 125),
        AVERAGE_IMPORTANT(50, 75),
        LESS_IMPORTANT(40, 75),
        LESS_LESS_IMPORTANT(30, 15),
        SMALL_IMPORTANT(20, 7),
        ALMOST_NOT_IMPORTANT(10, 3),
        NOT_IMPORTANT(0, 1);

        private final int value;
        private final int scaleFactor;

        public RdfLiteral.NumericLiteral getValueLiteral() {
            return Rdf.literalOf(value);
        }

        public RdfLiteral.NumericLiteral getScaleFactorLiteral() {
            return Rdf.literalOf(scaleFactor);
        }
    }

    private Expression<?> buildPreferenceWeightExpression(Variable intScorePreferenceVar) {
        Expression<?> expression = null; // base case

        List<PreferenceWeight> preferenceWeights = Arrays.stream(PreferenceWeight.values()).sorted(Comparator.comparingInt(PreferenceWeight::getValue))
                .toList();
        for (PreferenceWeight weight : preferenceWeights) {
            if (weight == PreferenceWeight.NOT_IMPORTANT) {
                expression = Expressions.iff(
                        Expressions.gt(intScorePreferenceVar, weight.getValueLiteral()),
                        weight.getScaleFactorLiteral(),
                        Rdf.literalOf(1)
                );
            } else {
                expression = Expressions.iff(
                        Expressions.gt(intScorePreferenceVar, weight.getValueLiteral()),
                        weight.getScaleFactorLiteral(),
                        expression
                );
            }
        }

        return expression;
    }

    Variable totalWeightedDeltaScore = SparqlBuilder.var("totalWeightedDeltaScore");
    Variable weightedDeltaScore = SparqlBuilder.var("weightedDeltaScore");


    private SubSelect getBiggerThanUserQualitiesQuery(RecommendationParameters parameters, IRI userId) {
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

        Variable featureName = SparqlBuilder.var("featureName");

        // ^hasFeature
        RdfPredicate reverseHasFeatureIRI = () -> "^<" + AttributeNames.Properties.HAS_FEATURE.pseudoUri() + ">";

        Expression<?> replaceExpr = Expressions.function(
                SparqlFunction.REPLACE,
                Expressions.str(hasFQuality),
                Rdf.literalOf("^.*/"),
                Rdf.literalOf("")
        );


        Variable intScoreRegionVar = SparqlBuilder.var("intScoreRegion");
        Bind bindRegionScore = Expressions.bind(() ->
                "<" +  CoreDatatype.XSD.INTEGER.getIri().stringValue() + ">" + "(" + scoreRegion.getQueryString() + ")", intScoreRegionVar);

        Variable intScorePreferenceVar = SparqlBuilder.var("intScorePreference");
        Bind bindPreferenceScore = Expressions.bind(() ->
               "<" + CoreDatatype.XSD.INTEGER.getIri().stringValue() + ">" + "(" + scorePreference.getQueryString() + ")", intScorePreferenceVar);

        Variable preferenceWeight = SparqlBuilder.var("preferenceWeight");
        Bind preferenceWeightBind = Expressions.bind(buildPreferenceWeightExpression(intScorePreferenceVar), preferenceWeight);

        Variable deltaScore = SparqlBuilder.var("deltaScore");
        Bind deltaScoreBind = Expressions.bind(Expressions.multiply(Expressions.subtract(intScoreRegionVar, intScorePreferenceVar), preferenceWeight), deltaScore);
        GraphPatternNotTriples bindScoreTriple = GraphPatterns.and(deltaScoreBind);
        GraphPatternNotTriples bindScores = GraphPatterns.and(bindRegionScore, bindPreferenceScore);

        var bindUserIri = GraphPatterns.and().values(builder -> {
            builder.variables(user);
            builder.value(Rdf.iri(userId));
        });

        return GraphPatterns.select(user, region, Expressions.count(hasFQuality).distinct().as(matchingPreferences),
                        Expressions.group_concat("\"%s\"".formatted(WORDS_SEPARATOR), replaceExpr).distinct().as(aggregateOfFeatures),
                        Expressions.divide(Expressions.sum(deltaScore), Expressions.avg(deltaScore)).as(avgDeltaScore),
                        Expressions.sum(preferenceWeight).as(totalWeightedDeltaScore)
                )
                .where(
                        bindUserIri,
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
                        GraphPatterns.tp(featureRegion, AttributeNames.Properties.HAS_REGION_FEATURE.rdfIri(), featureName),
                        GraphPatterns.tp(someRegion, vf.createIRI(RegionNames.Properties.SF_WITHIN), region),
                        // USER_PREFERENCE triples
                        GraphPatterns.tp(preference, RDF.TYPE, UserNames.Classes.USER_WITH_PREFERENCE.rdfIri()),
                        GraphPatterns.tp(preference, DC.CREATOR, user),
                        GraphPatterns.tp(preference, hasFQuality, qualityPreference),
                        GraphPatterns.tp(hasFQuality, RegionNames.Properties.FOR_FEATURE.rdfIri(), featurePreference),
                        GraphPatterns.tp(featurePreference, AttributeNames.Properties.HAS_SCORE.rdfIri(), scorePreference),
                        GraphPatterns.tp(featurePreference, reverseHasFeatureIRI, preference),
                        GraphPatterns.tp(featurePreference, AttributeNames.Properties.HAS_REGION_FEATURE.rdfIri(), featureName),
                        // bindings for the parameters into recommendation
                        GraphPatterns.and().values(builder -> {
                            builder.variables(tolerance);
                            builder.value(toleranceLiteral);
                        }),
                        bindScores,
                        preferenceWeightBind,
                        bindScoreTriple,
                        GraphPatterns.and().filter(Expressions.gt(intScoreRegionVar, Expressions.add(intScorePreferenceVar, tolerance)))
                )
                .groupBy(user, region);
    }

    public String simpleRecommendationQuery(IRI userId) {
        //   ?region a :SimpleRecommendation
        TriplePattern regionIsRecommendation = GraphPatterns
                .tp(region, RDF.TYPE, RecommendationNames.Classes.SIMPLE_RECOMMENDATION.rdfIri());

        //  ?region :recommendedFor ?user
        TriplePattern recommendedFor = GraphPatterns
                .tp(region, RecommendationNames.Properties.RECOMMENDED_FOR.rdfIri(), user);

        //    SELECT ?user (COUNT(*) AS ?totalPrefs)
        SubSelect userQualitiesQuery = getUserQualitiesQuery(userId);
        SubSelect regionQualitiesQuery = getExactRegionUserQualitiesQuery();

        GraphPatternNotTriples filter = GraphPatterns.and()
                .filter(Expressions.equals(totalPreferences, matchingPreferences));


        ConstructQuery constructQuery = Queries.CONSTRUCT(regionIsRecommendation, recommendedFor)
                .where(userQualitiesQuery, regionQualitiesQuery, filter);

        return constructQuery.getQueryString();
    }

    public String biggerThanRecommendationQuery(RecommendationParameters parameters, IRI userId) {
        TriplePattern regionIsRecommendation = GraphPatterns
                .tp(region, RDF.TYPE, RecommendationNames.Classes.BIGGER_THAN_RECOMMENDATION.rdfIri());

        TriplePattern recommendedFor = GraphPatterns
                .tp(region, RecommendationNames.Properties.RECOMMENDED_FOR.rdfIri(), user);

        SubSelect userQualitiesQuery = getUserQualitiesQueryUpdated(userId);
        SubSelect regionQualitiesQuery = getBiggerThanUserQualitiesQuery(parameters, userId);

        Variable minMatchRatio = SparqlBuilder.var("minMatchRatio");
        GraphPattern addMatchRatioToGraph = GraphPatterns.and().values(builder -> {
            builder.variables(minMatchRatio);
            builder.value(Rdf.literalOf(parameters.getMatchRatio()));
        });

        Bind weightedDeltaScoreBind = Expressions.bind(Expressions.divide(totalWeightedDeltaScore, totalPreferences), weightedDeltaScore);

        Variable matchRatio = SparqlBuilder.var("matchRatio");
        Bind matchRatioBind = Expressions.bind(Expressions.divide(matchingPreferences, totalPreferences), matchRatio);
        GraphPatternNotTriples filter = GraphPatterns
                .and(weightedDeltaScoreBind)
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
            TriplePattern explanationType = GraphPatterns.tp(explanation, RDF.TYPE, RecommendationNames.Classes.EXPLANATION.rdfIri());
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
                    .orderBy(SparqlBuilder.desc(weightedDeltaScore), SparqlBuilder.desc(matchRatio))
                    .limit(parameters.getMaxResults());
            queryString = constructQuery.getQueryString();
        } else {
            ConstructQuery constructQuery = Queries.CONSTRUCT(
                            regionIsRecommendation,
                            recommendedFor,
                            confidenceLevel
                    )
                    .where(userQualitiesQuery, regionQualitiesQuery, addMatchRatioToGraph, filter)
                    .orderBy(SparqlBuilder.desc(weightedDeltaScore), SparqlBuilder.desc(matchRatio))
                    .limit(parameters.getMaxResults());
            queryString = constructQuery.getQueryString();
        }

        return queryString;
    }
}
