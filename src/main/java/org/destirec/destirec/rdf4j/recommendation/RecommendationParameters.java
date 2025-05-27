package org.destirec.destirec.rdf4j.recommendation;

import lombok.Getter;

@Getter
public class RecommendationParameters {
    /**
     * tolerance - allow algorithm to select in the neighborhood of the score, soft margin
     *               allowed from -10 to 10
     */
    private final short tolerance;
    /**
     * matchRatio - select percentage of how many features should match with the user defined
     * ones
     */
    private final float matchRatio;


    /**
     * maxResults - take top N recommendations only
     */
    private final int maxResults;

    /**
     * explanations can help with the understanding why recommendation was made
     */
    private final boolean addExplanations;

    public RecommendationParameters(Short tolerance, Float matchRatio, Integer maxResults, Boolean addExplanations) {
        this.tolerance = tolerance == null ? 0 : tolerance;
        this.matchRatio = matchRatio == null ? 1 : matchRatio;
        this.maxResults = maxResults == null ? 255 : maxResults;
        this.addExplanations = addExplanations != null && addExplanations;
    }
}
