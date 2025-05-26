package org.destirec.destirec.rdf4j.knowledgeGraph;

import lombok.*;
import org.destirec.destirec.rdf4j.region.apiDto.SimpleRegionDto;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA.RegionOntology.QTypes;
import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames.Individuals.RegionFeature;
import org.javatuples.Pair;
import org.javatuples.Quartet;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class POIClass extends SimpleRegionDto {
    private RegionFeature feature;
    private QTypes featureSpecificType;

    private static volatile int SITE_LINKS_MAX = 0;
    private static volatile int SITE_LINKS_MIN = 0;
    private static volatile int OUTGOING_LINKS_MAX = 0;
    private static volatile int OUTGOING_LINKS_MIN = 0;
    private static volatile int STATEMENTS_MAX = 0;
    private static volatile int STATEMENTS_MIN = 0;
    private static final short INTERNET_POPULARITY_MAX = 4;
    private static final short INTERNET_POPULARITY_MIN = 0;


    // properties for heuristic score
    @Setter
    private String osmLink;

    private int siteLinks;

    private boolean hasImage;

    @Setter
    private String officialWebsite;

    private Pair<String, String> images;
    private int outgoingLinks;
    private int statements;
    private short internetPopularity;

    private boolean hasQuoraTopic;
    private boolean hasTwitterAccount;
    private boolean hasImdbKeyword;
    private boolean hasTripAdvisorAccount;
    private Quartet<String, String, String, String> internetMentions;

    @Setter
    private String coords;

    public void setImages(String wikidata, String dbpedia) {
        if (wikidata == null && dbpedia == null) {
            this.hasImage = false;
        }
        this.images = new Pair<>(wikidata, dbpedia);
        this.hasImage = true;
    }

    public void setSiteLinks(int siteLinks) {
        if (siteLinks < SITE_LINKS_MIN) {
            SITE_LINKS_MIN = siteLinks;
        } else if (siteLinks > SITE_LINKS_MAX) {
            SITE_LINKS_MAX = siteLinks;
        }
        this.siteLinks = siteLinks;
    }

    public void setInternetMentions(String quora, String tripAdvisor, String twitter, String imdb) {
        this.internetMentions = new Quartet<>(quora, tripAdvisor, twitter, imdb);
        this.hasQuoraTopic = quora != null;
        this.hasTripAdvisorAccount = tripAdvisor != null;
        this.hasTwitterAccount = twitter != null;
        this.hasImdbKeyword = imdb != null;

        this.internetPopularity = (short) (
                (hasQuoraTopic ? 1 : 0) +
                (hasTripAdvisorAccount ? 1 : 0) +
                (hasTwitterAccount ? 1 : 0) +
                (hasImdbKeyword ? 1 : 0)
        );
    }

    public void setOutgoingLinks(int outgoingLinks) {
        if (outgoingLinks < OUTGOING_LINKS_MIN) {
            OUTGOING_LINKS_MIN = outgoingLinks;
        } else if (outgoingLinks > OUTGOING_LINKS_MAX) {
            OUTGOING_LINKS_MAX = outgoingLinks;
        }
        this.outgoingLinks = outgoingLinks;
    }

    public void setStatements(int statements) {
        if (statements < STATEMENTS_MIN) {
            STATEMENTS_MIN = statements;
        } else if (statements > STATEMENTS_MAX) {
            STATEMENTS_MAX = statements;
        }
        this.statements = statements;
    }

    public void setFeature(QTypes featureSpecificType) {
        this.feature = QTypes.getRegionFeature(featureSpecificType);
        this.featureSpecificType = featureSpecificType;
    }

    private static double normalizeValue(int value, int max, int min) {
        if (max == min) {
            return 0.0;
        }
        return (value - min) / (double) (max - min);
    }

    public double getHeuristicScore() {
        return normalizeValue(statements, STATEMENTS_MAX, STATEMENTS_MIN) +
               normalizeValue(internetPopularity, INTERNET_POPULARITY_MAX, INTERNET_POPULARITY_MIN) +
               normalizeValue(outgoingLinks, OUTGOING_LINKS_MAX, OUTGOING_LINKS_MIN) +
               normalizeValue(siteLinks, SITE_LINKS_MAX, SITE_LINKS_MIN) +
               normalizeValue(hasImage ? 1 : 0, 1, 0) +
               normalizeValue(officialWebsite != null ? 1 : 0, 1, 0) +
               normalizeValue(coords != null ? 1 : 0, 1, 0);
    }

    public double getPercentageScore() {
        return (getHeuristicScore() * 100) / 7.0;
    }
}
