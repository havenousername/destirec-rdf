package org.destirec.destirec.rdf4j.recommendation;

import lombok.Getter;
import lombok.Setter;
import org.destirec.destirec.utils.rdfDictionary.RegionNames.Individuals.RegionTypes;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

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
    @Setter
    private IRI fromRegion;

    @Setter
    private RegionTypes fromRegionType;
    private final RegionTypes toRegionType;

    public RecommendationParameters(Short tolerance, Float matchRatio, Integer maxResults, Boolean addExplanations, String fromRegion, String toRegionType) {
        this.tolerance = tolerance == null ? 0 : tolerance;
        this.matchRatio = matchRatio == null ? 1 : matchRatio;
        this.maxResults = maxResults == null ? 255 : maxResults;
        this.addExplanations = addExplanations != null && addExplanations;
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        this.fromRegion = fromRegion != null ? vf.createIRI(fromRegion) : null;
        RegionTypes toRegionTypeInput;
        try {
            toRegionTypeInput = RegionTypes.fromString(toRegionType);
        } catch (IllegalArgumentException e) {
            toRegionTypeInput = RegionTypes.DISTRICT;
        }
        this.toRegionType = toRegionTypeInput;
    }

    public static RecommendationParameters getDefault() {
        return new RecommendationParameters(
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
