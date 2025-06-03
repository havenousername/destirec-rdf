package org.destirec.destirec.rdf4j.recommendation;

import org.apache.http.client.utils.URIBuilder;
import org.destirec.destirec.rdf4j.poi.POIDao;
import org.destirec.destirec.rdf4j.preferences.PreferenceDao;
import org.destirec.destirec.rdf4j.preferences.PreferenceDto;
import org.destirec.destirec.rdf4j.region.RegionDao;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.user.UserDao;
import org.destirec.destirec.rdf4j.user.UserDto;
import org.destirec.destirec.utils.ShortRepositoryInfo;
import org.destirec.destirec.utils.SimpleDtoTransformations;
import org.destirec.destirec.utils.URIHandling;
import org.destirec.destirec.utils.rdfDictionary.RecommendationNames;
import org.destirec.destirec.utils.rdfDictionary.RecommendationNames.Individuals.RecommendationStrategies;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final UserDao userDao;

    private final RegionDao regionDao;

    private final POIDao poiDao;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private final ShortRepositoryInfo repositoryInfo;

    private final RecommendationQueries recommendationQueries;

    private LinearAlgebraRecommendation linearAlgebraRecommendation;

    private final PreferenceDao preferenceDao;

    public RecommendationService(
            UserDao userDao,
            RegionDao regionDao, POIDao poiDao,
            RecommendationQueries recommendationQueries,
            ShortRepositoryInfo repositoryInfo, PreferenceDao preferenceDao
    ) {
        this.userDao = userDao;
        this.regionDao = regionDao;
        this.poiDao = poiDao;
        this.recommendationQueries = recommendationQueries;
        this.repositoryInfo = repositoryInfo;
        this.preferenceDao = preferenceDao;
    }


    private Recommendation handleRecommendationQuery(
            String query,
            RecommendationStrategies strategy,
            RecommendationParameters parameters
    ) {
        GraphQueryResult result = userDao.getRdf4JTemplate().applyToConnection(connection -> {
            var tupleQueryReady = connection.prepareGraphQuery(query);
            return tupleQueryReady.evaluate();
        });

        List<RecommendationEntity> recommendations = new ArrayList<>();
        RegionDto tmpRegion = null;
        UserDto tmpUser = null;
        float confidenceLevel = 1f;
        int iteration = 0;
        while (result.hasNext()) {
            Statement statement = result.next();
            ValueFactory factory = SimpleValueFactory.getInstance();
            if (statement.getPredicate().equals(RDF.TYPE) && statement.getObject().equals(strategy.iri().rdfIri())) {
                IRI regionIRI = factory.createIRI(statement.getSubject().stringValue());
                tmpRegion = regionDao.getById(regionIRI);
            } else if (statement.getPredicate().equals(RecommendationNames.Properties.RECOMMENDED_FOR.rdfIri())) {
                IRI userIRI = factory.createIRI(statement.getObject().stringValue());
                tmpUser = userDao.getById(userIRI);
            } else if (statement.getPredicate().equals(RecommendationNames.Properties.CONFIDENCE_LEVEL.rdfIri())) {
                confidenceLevel = Float.parseFloat(statement.getObject().stringValue());
                if (!recommendations.isEmpty()) {
                    recommendations.getLast().setConfidence(confidenceLevel);
                }
            } else if (statement.getPredicate().equals(RecommendationNames.Properties.HAS_EXPLANATION.rdfIri())) {
                result.next();
                String explanationType = statement.getObject().stringValue();
                statement = result.next();
                List<String> features = SimpleDtoTransformations
                        .toListString(statement.getObject().stringValue(), RecommendationQueries.WORDS_SEPARATOR)
                        .stream()
                        .map(String::toLowerCase)
                        .map(String::trim)
                        .map((s) -> s.replace("has", "").replace("quality", ""))
                        .collect(Collectors.toList());
                statement = result.next();
                float deltaScore = Float.parseFloat(statement.getObject().stringValue());
                RecommendationEntity.RecommendationExplanation explanation =
                        new RecommendationEntity.RecommendationExplanation(explanationType, features, deltaScore);
                if (!recommendations.isEmpty()) {
                    recommendations.getLast().setExplanation(explanation);
                }
            }

            if (tmpRegion != null && tmpUser != null) {
                iteration++;
                recommendations.add(new RecommendationEntity(iteration, tmpRegion, tmpUser, confidenceLevel, null));
                tmpRegion = null;
                tmpUser = null;
                confidenceLevel = 1f;
            }
        }
        String workbenchURL = "";
        if (repositoryInfo.isRemote()) {
            try {
                URI uri = getUri(query);
                workbenchURL = uri.toURL().toExternalForm();
            } catch (URISyntaxException e) {
                logger.error("Could not parse repository URL", e);
            } catch (MalformedURLException e) {
                logger.error("URL is invalid", e);
            }
        }
        return new Recommendation(
                strategy.getName(),
                recommendations,
                query.replaceAll("\n", ""),
                workbenchURL,
                parameters
        );
    }


    private URI getUri(String query) throws URISyntaxException {
        URI originalUri = URI.create(repositoryInfo.getLocation());
        URIBuilder uriBuilder = new URIBuilder(URIHandling.getOrigin(originalUri.toString()) + "/graphs-visualizations");
        uriBuilder.addParameter("embedded", "true");
        uriBuilder.addParameter("locationChangeSuccess", "false");
        uriBuilder.addParameter("searchVisible", "false");
        uriBuilder.addParameter("queryResultsMode", "true");
        uriBuilder.addParameter("noGoHome", "true");
        uriBuilder.addParameter("query", query.replaceAll("\n", ""));

        return uriBuilder.build();
    }

    public Recommendation getSimpleRecommendation(RecommendationParameters parameters) {
        String query = recommendationQueries.simpleRecommendationQuery();
        return handleRecommendationQuery(query, RecommendationStrategies.SIMPLE_RECOMMENDATION, parameters);
    }

    public Recommendation getBiggerThanRecommendation(RecommendationParameters parameters) {
        if (parameters.getFromRegion() == null) {
            Optional<IRI> parentByType = regionDao.getByType(RegionNames.Individuals.RegionTypes.WORLD);
            if (parentByType.isPresent()) {
                parameters.setFromRegionParameter(parentByType.get());
                parameters.setFromRegionType(RegionNames.Individuals.RegionTypes.WORLD);
            } else {
                throw new RuntimeException("World individual should be presented in the database");
            }
        } else {
            RegionDto parentRegion = regionDao.getById(parameters.getFromRegion());
            parameters.setFromRegionType(parentRegion.getType());
        }

        String query = recommendationQueries.biggerThanRecommendationQuery(parameters);
        return handleRecommendationQuery(query, RecommendationStrategies.BIGGER_THAN_RECOMMENDATION, parameters);
    }


    public Void getLARecommendation(RecommendationParameters parameters) {
        Recommendation recommendation = getBiggerThanRecommendation(parameters);
        linearAlgebraRecommendation = new LinearAlgebraRecommendation(recommendation.entities(), regionDao, poiDao);

        IRI author = userDao.list().getFirst().id();
        Optional<PreferenceDto> preferenceDto = preferenceDao.getByAuthor(author);
        if (preferenceDto.isEmpty()) {
            throw new RuntimeException("Should have at least one user preference in the database for this operation");
        }
        linearAlgebraRecommendation.createRPOIMatrix();
        linearAlgebraRecommendation.createRFQMatrix();
        linearAlgebraRecommendation.calculateLatentSpace(preferenceDto.get());
        return null;
    }
}
