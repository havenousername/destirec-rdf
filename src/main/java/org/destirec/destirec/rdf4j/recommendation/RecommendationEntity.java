package org.destirec.destirec.rdf4j.recommendation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.user.UserDto;
import org.eclipse.rdf4j.model.IRI;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

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
    @Nullable
    private RecommendationExplanationPOI poiExplanation;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class RecommendationExplanation {
        private String explanationType;
        private List<String> forFeatures;
        private float avgDelta;
        private float scoreWeight;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class RecommendationExplanationPOI {
        private List<IRI> features;
        private List<Pair<IRI, IRI>> pois;
    }
}
