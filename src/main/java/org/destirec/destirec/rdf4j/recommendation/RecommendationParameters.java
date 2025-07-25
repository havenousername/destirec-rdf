package org.destirec.destirec.rdf4j.recommendation;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.IriSerializer;
import org.destirec.destirec.utils.rdfDictionary.RegionNames.Individuals.RegionTypes;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

@Getter
public class RecommendationParameters extends SimpleRecommendationParameters {
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

    @JsonSerialize(using = IriSerializer.class)
    private IRI fromRegion;

    @Setter
    private RegionTypes fromRegionType;

    private final RegionTypes toRegionType;

    private static boolean isEmpty(String entity) {
        return entity != null && !entity.isEmpty() && !entity.equalsIgnoreCase("null") &&
                !entity.equalsIgnoreCase("undefined") && !entity.equalsIgnoreCase("none") && !entity.equalsIgnoreCase("empty") && !entity.equalsIgnoreCase("blank");
    }

    public RecommendationParameters(Short tolerance, Float matchRatio, Integer maxResults, Boolean addExplanations, String fromRegion, String toRegionType) {
        super(addExplanations);
        this.tolerance = tolerance == null ? 0 : tolerance;
        this.matchRatio = matchRatio == null ? 1 : matchRatio;
        this.maxResults = maxResults == null ? 255 : maxResults;
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        this.fromRegion = isEmpty(fromRegion) ?
                vf.createIRI(DESTIREC.wrapNamespace( "region/", DESTIREC.UriType.RESOURCE) + fromRegion) : null;
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

    public void setFromRegionParameter(IRI fromRegion) {
        this.fromRegion = fromRegion;
    }
}
