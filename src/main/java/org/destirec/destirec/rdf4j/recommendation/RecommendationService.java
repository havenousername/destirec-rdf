package org.destirec.destirec.rdf4j.recommendation;

import org.destirec.destirec.rdf4j.region.RegionDao;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.user.UserDao;
import org.destirec.destirec.rdf4j.user.UserDto;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.RecommendationNames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.UserNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ConstructQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.SubSelect;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {
    private final RDF4JTemplate template;

    private final UserDao userDao;

    private final RegionDao regionDao;


    protected Logger logger = LoggerFactory.getLogger(getClass());

    public RecommendationService(RDF4JTemplate template, UserDao userDao, RegionDao regionDao) {
        this.template = template;
        this.userDao = userDao;
        this.regionDao = regionDao;
    }

    private String simpleRecommendationQuery() {
        Variable region = SparqlBuilder.var("region");
        Variable user = SparqlBuilder.var("user");

        //   ?region a :SimpleRecommendation
        TriplePattern regionIsRecommendation = GraphPatterns
                .tp(region, RDF.TYPE, RecommendationNames.Classes.SIMPLE_RECOMMENDATION.rdfIri());

        //  ?region :recommendedFor ?user
        TriplePattern recommendedFor = GraphPatterns
                .tp(region, RecommendationNames.Properties.RECOMMENDED_FOR.rdfIri(), user);

        Variable totalPreferences = SparqlBuilder.var("totalPreferences");

        //    SELECT ?user (COUNT(*) AS ?totalPrefs)

        Variable preference = SparqlBuilder.var("preference");
        Variable hasFQuality = SparqlBuilder.var("hasFQuality");
        Variable quality = SparqlBuilder.var("feature");
        SubSelect userQualitiesQuery = GraphPatterns.select(user, Expressions.countAll().as(totalPreferences))
                .where(
                        GraphPatterns.tp(preference, RDF.TYPE, UserNames.Classes.USER_WITH_PREFERENCE.rdfIri()),
                        GraphPatterns.tp(preference, DC.CREATOR, user),
                        GraphPatterns.tp(preference, hasFQuality, quality),
                        GraphPatterns.tp(hasFQuality, RDFS.SUBPROPERTYOF, AttributeNames.Properties.HAS_QUALITY.rdfIri()))
                .groupBy(user);

        Variable matchingPreferences = SparqlBuilder.var("matchingPreferences");
        SubSelect regionQualitiesQuery = GraphPatterns.select(user, region, Expressions.countAll().as(matchingPreferences))
                .where(
                        GraphPatterns.tp(preference, RDF.TYPE, UserNames.Classes.USER_WITH_PREFERENCE.rdfIri()),
                        GraphPatterns.tp(preference, DC.CREATOR, user),
                        GraphPatterns.tp(preference, hasFQuality, quality),
                        GraphPatterns.tp(hasFQuality, RDFS.SUBPROPERTYOF, AttributeNames.Properties.HAS_QUALITY.rdfIri()),
                        GraphPatterns.tp(region, RDF.TYPE, RegionNames.Classes.LEAF_REGION.rdfIri()),
                        GraphPatterns.tp(region, hasFQuality, quality)
                )
                .groupBy(user, region);

        GraphPatternNotTriples filter = GraphPatterns.and().filter(Expressions.equals(totalPreferences, matchingPreferences));


        ConstructQuery constructQuery = Queries.CONSTRUCT(regionIsRecommendation, recommendedFor)
                .where(userQualitiesQuery, regionQualitiesQuery, filter);

        return constructQuery.getQueryString();
    }


    public Recommendation getSimpleRecommendation() {
        String query = simpleRecommendationQuery();
        GraphQueryResult result = userDao.getRdf4JTemplate().applyToConnection(connection -> {
            var tupleQueryReady = connection.prepareGraphQuery(query);
            return tupleQueryReady.evaluate();
        });

        boolean isFirstIt = false;

        List<Pair<UserDto, RegionDto>> recommendations = new ArrayList<>();
        while (result.hasNext()) {
            Statement statement = result.next();
            if (!isFirstIt) {
                if (!statement.getObject().equals(RecommendationNames.Classes.SIMPLE_RECOMMENDATION.rdfIri())) {
                    throw new RuntimeException("Should return simple recommendation only class!." +
                            "Returned " + statement.getObject());
                }
            } else {
                ValueFactory factory = SimpleValueFactory.getInstance();
                IRI regionIRI = factory.createIRI(statement.getSubject().stringValue());
                IRI userIRI = factory.createIRI(statement.getObject().stringValue());
                RegionDto regionDto = regionDao.getById(regionIRI);
                UserDto userDto = userDao.getById(userIRI);
                recommendations.add(new Pair<>(userDto, regionDto));
            }
            isFirstIt = true;
        }

        return new Recommendation(RecommendationNames.Classes.SIMPLE_RECOMMENDATION.str(), recommendations);
    }
}
