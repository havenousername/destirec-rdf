package org.destirec.destirec.rdf4j.recommendation;

import lombok.Getter;

@Getter
public class SimpleRecommendationParameters {
    /**
     * explanations can help with the understanding why recommendation was made
     */
    private final boolean addExplanations;


    public SimpleRecommendationParameters(Boolean addExplanations) {
        this.addExplanations = addExplanations != null && addExplanations;
    }
}
