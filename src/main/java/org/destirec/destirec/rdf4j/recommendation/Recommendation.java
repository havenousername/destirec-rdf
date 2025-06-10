package org.destirec.destirec.rdf4j.recommendation;

import java.util.List;

public record Recommendation(
        String type,
        List<RecommendationEntity> entities,
        String query,
        String workbenchGraphURL,
        SimpleRecommendationParameters parameters
) { }

