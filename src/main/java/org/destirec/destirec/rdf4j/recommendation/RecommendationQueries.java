package org.destirec.destirec.rdf4j.recommendation;

import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.RecommendationNames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.UserNames;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ConstructQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.SubSelect;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.springframework.stereotype.Repository;

@Repository
public class RecommendationQueries {

    private final Variable region = SparqlBuilder.var("region");
    private final Variable user = SparqlBuilder.var("user");
    private final Variable totalPreferences = SparqlBuilder.var("totalPreferences");

    private final Variable matchingPreferences = SparqlBuilder.var("matchingPreferences");

    private final Variable preference = SparqlBuilder.var("preference");
    private final Variable hasFQuality = SparqlBuilder.var("hasFQuality");


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

    private SubSelect getBiggerThanUserQualitiesQuery() {
        Variable qualityRegion = SparqlBuilder.var("qualityRegion");
        Variable featureRegion = SparqlBuilder.var("featureRegion");
        Variable scoreRegion = SparqlBuilder.var("scoreRegion");

        Variable qualityPreference = SparqlBuilder.var("qualityPreference");
        Variable featurePreference = SparqlBuilder.var("featurePreference");
        Variable scorePreference = SparqlBuilder.var("scorePreference");
        return GraphPatterns.select(user, region, Expressions.count(hasFQuality).distinct().as(matchingPreferences))
                .where(
                        // REGION triples
                        GraphPatterns.tp(region, RDF.TYPE, RegionNames.Classes.LEAF_REGION.rdfIri()),
                        GraphPatterns.tp(region, hasFQuality, qualityRegion),
                        GraphPatterns.tp(hasFQuality, RDFS.SUBPROPERTYOF, AttributeNames.Properties.HAS_QUALITY.rdfIri()),
                        GraphPatterns.tp(hasFQuality, RegionNames.Properties.FOR_FEATURE.rdfIri(), featureRegion),
                        GraphPatterns.tp(featureRegion, AttributeNames.Properties.HAS_SCORE.rdfIri() ,scoreRegion),
                        GraphPatterns.tp(featureRegion, AttributeNames.Properties.HAS_FEATURE.reverseRdfIri(), region),
                        // USER_PREFERENCE triples
                        GraphPatterns.tp(preference, RDF.TYPE, UserNames.Classes.USER_WITH_PREFERENCE.rdfIri()),
                        GraphPatterns.tp(preference, DC.CREATOR, user),
                        GraphPatterns.tp(preference, hasFQuality, qualityPreference),
                        GraphPatterns.tp(hasFQuality, RegionNames.Properties.FOR_FEATURE.rdfIri(), featurePreference),
                        GraphPatterns.tp(featurePreference, AttributeNames.Properties.HAS_SCORE.rdfIri(), scorePreference),
                        GraphPatterns.tp(featurePreference, AttributeNames.Properties.HAS_FEATURE.reverseRdfIri(), preference),
                        GraphPatterns.and().filter(Expressions.gt(scoreRegion, scorePreference))
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

        GraphPatternNotTriples filter = GraphPatterns.and().filter(Expressions.equals(totalPreferences, matchingPreferences));


        ConstructQuery constructQuery = Queries.CONSTRUCT(regionIsRecommendation, recommendedFor)
                .where(userQualitiesQuery, regionQualitiesQuery, filter);

        return constructQuery.getQueryString();
    }

    public String biggerThanRecommendationQuery() {
        TriplePattern regionIsRecommendation = GraphPatterns
                .tp(region, RDF.TYPE, RecommendationNames.Classes.BIGGER_THAN_RECOMMENDATION.rdfIri());

        TriplePattern recommendedFor = GraphPatterns
                .tp(region, RecommendationNames.Properties.RECOMMENDED_FOR.rdfIri(), user);

        SubSelect userQualitiesQuery = getUserQualitiesQuery();
        SubSelect regionQualitiesQuery = getBiggerThanUserQualitiesQuery();

        GraphPatternNotTriples filter = GraphPatterns.and().filter(Expressions.equals(totalPreferences, matchingPreferences));

        ConstructQuery constructQuery = Queries.CONSTRUCT(regionIsRecommendation, recommendedFor)
                .where(userQualitiesQuery, regionQualitiesQuery, filter);
        return constructQuery.getQueryString();
    }
}
