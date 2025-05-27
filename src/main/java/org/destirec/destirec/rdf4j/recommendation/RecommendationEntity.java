package org.destirec.destirec.rdf4j.recommendation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.user.UserDto;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class RecommendationEntity {
    private int priority;
    private RegionDto region;
    private UserDto user;
    private float confidence;
    private RecommendationExplanation explanation;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class RecommendationExplanation {
        private String explanationType;
        private List<String> forFeatures;
        private float avgDelta;
    }
}
