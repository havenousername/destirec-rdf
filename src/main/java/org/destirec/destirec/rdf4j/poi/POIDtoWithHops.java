package org.destirec.destirec.rdf4j.poi;

import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA;
import org.eclipse.rdf4j.model.IRI;
import org.javatuples.Pair;
import org.javatuples.Quartet;

public class POIDtoWithHops extends POIDto {
    public POIDtoWithHops(
            IRI id,
            String name,
            IRI source,
            IRI parentRegion,
            FeatureDto feature,
            WIKIDATA.RegionOntology.QTypes featureSpecificType,
            String osmLink,
            int siteLinks,
            boolean hasImage,
            String officialWebsite,
            Pair<String, String> images,
            int outgoingLinks,
            int statements,
            boolean hasQuoraTopic,
            boolean hasTwitterAccount,
            boolean hasImdbKeyword,
            boolean hasTripAdvisorAccount,
            Quartet<String, String, String, String> internetMentions,
            String coords,
            int percentageScore,
            IRI ancestor,
            int hopCount
    ) {
        super(id, name, source, parentRegion, feature, featureSpecificType, osmLink, siteLinks, hasImage, officialWebsite, images, outgoingLinks, statements, hasQuoraTopic, hasTwitterAccount, hasImdbKeyword, hasTripAdvisorAccount, internetMentions, coords, percentageScore);
        this.ancestor = ancestor;
        this.hopCount = hopCount;
    }

    public POIDtoWithHops(POIDto base, IRI ancestor, int hopCount) {
        super(
                base.getId(),
                base.getName(),
                base.getSource(),
                base.getParentRegion(),
                base.getFeature(),
                base.getFeatureSpecificType(),
                base.getOsmLink(),
                base.getSiteLinks(),
                base.isHasImage(),
                base.getOfficialWebsite(),
                base.getImages(),
                base.getOutgoingLinks(),
                base.getStatements(),
                base.isHasQuoraTopic(),
                base.isHasTwitterAccount(),
                base.isHasImdbKeyword(),
                base.isHasTripAdvisorAccount(),
                base.getInternetMentions(),
                base.getCoords(),
                base.getPercentageScore()
        );
        this.ancestor = ancestor;
        this.hopCount = hopCount;
    }

    private final IRI ancestor;
    private final int hopCount;
}
