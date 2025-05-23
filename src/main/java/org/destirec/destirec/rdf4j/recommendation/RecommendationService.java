package org.destirec.destirec.rdf4j.recommendation;

import org.destirec.destirec.rdf4j.region.RegionDao;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.user.UserDao;
import org.destirec.destirec.rdf4j.user.UserDto;
import org.destirec.destirec.utils.rdfDictionary.RecommendationNames.Individuals.RecommendationStrategies;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {

    private final UserDao userDao;

    private final RegionDao regionDao;


    protected Logger logger = LoggerFactory.getLogger(getClass());

    private final RecommendationQueries recommendationQueries;

    public RecommendationService(UserDao userDao, RegionDao regionDao, RecommendationQueries recommendationQueries) {
        this.userDao = userDao;
        this.regionDao = regionDao;
        this.recommendationQueries = recommendationQueries;
    }


    private Recommendation handleRecommendationQuery(
            String query,
            RecommendationStrategies strategy
    ) {
        GraphQueryResult result = userDao.getRdf4JTemplate().applyToConnection(connection -> {
            var tupleQueryReady = connection.prepareGraphQuery(query);
            return tupleQueryReady.evaluate();
        });

        boolean isFirstIt = false;

        List<Pair<UserDto, RegionDto>> recommendations = new ArrayList<>();
        while (result.hasNext()) {
            Statement statement = result.next();
            if (!isFirstIt) {
                if (!statement.getObject().stringValue().equals(strategy.getName())) {
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

        return new Recommendation(strategy.getName(), recommendations);
    }

    public Recommendation getSimpleRecommendation() {
        String query = recommendationQueries.simpleRecommendationQuery();
        return handleRecommendationQuery(query, RecommendationStrategies.SIMPLE_RECOMMENDATION);
    }

    public Recommendation getBiggerThanRecommendation() {
        String query = recommendationQueries.biggerThanRecommendationQuery();
        return handleRecommendationQuery(query, RecommendationStrategies.BIGGER_THAN_RECOMMENDATION);
    }
}
