package org.destirec.destirec.rdf4j.poi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.destirec.destirec.rdf4j.interfaces.ConfigFields;
import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA.RegionOntology.QTypes;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.javatuples.Pair;
import org.javatuples.Quartet;

import java.util.HashMap;
import java.util.Map;

@Builder
@AllArgsConstructor
@Getter
@ToString
public class POIDto implements Dto {
    private final IRI id;
    private final String name;
    private final IRI source;
    private final IRI parentRegion;
    private final FeatureDto feature;
    private final QTypes featureSpecificType;

    // Heuristic score properties
    private final String osmLink;
    private final int siteLinks;
    private final boolean hasImage;
    private final String officialWebsite;
    private final Pair<String, String> images;
    private final int outgoingLinks;
    private final int statements;
    private final boolean hasQuoraTopic;
    private final boolean hasTwitterAccount;
    private final boolean hasImdbKeyword;
    private final boolean hasTripAdvisorAccount;
    private final Quartet<String, String, String, String> internetMentions;
    private final String coords;
    private final int percentageScore;
    private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Override
    public Map<POIConfig.Field, String> getMap() {
        Map<ConfigFields.Field, String> map = new HashMap<>();

        // Basic properties
        map.put(POIConfig.Fields.NAME, name);
        map.put(POIConfig.Fields.SOURCE, source.stringValue());
        map.put(POIConfig.Fields.PARENT_REGION, parentRegion != null ? parentRegion.stringValue() : null);
        map.put(POIConfig.Fields.FEATURE, feature != null ? feature.id().stringValue() : null);
        map.put(POIConfig.Fields.FEATURE_SPECIFIC_TYPE, featureSpecificType != null ? featureSpecificType.iri().pseudoUri() : null);

        // Heuristic properties
        map.put(POIConfig.Fields.OSM, osmLink);
        map.put(POIConfig.Fields.SITE_LINKS_NUMBER, String.valueOf(siteLinks));
        map.put(POIConfig.Fields.OFFICIAL_WEBSITE, officialWebsite);

        if (images != null) {
            map.put(POIConfig.Fields.IMAGE, String.valueOf(images.getValue0()));
            map.put(POIConfig.Fields.THUMBNAIL, String.valueOf(images.getValue1()));
        }

        map.put(POIConfig.Fields.OUTGOING_LINKS_NUMBER, String.valueOf(outgoingLinks));
        map.put(POIConfig.Fields.WIKI_STATEMENTS, String.valueOf(statements));

        if (internetMentions != null) {
            map.put(POIConfig.Fields.QUORA_TOPIC_ID, internetMentions.getValue0());
            map.put(POIConfig.Fields.TRIPADVISOR_ID, internetMentions.getValue1());
            map.put(POIConfig.Fields.TWITTER_ID, internetMentions.getValue2());
            map.put(POIConfig.Fields.IMDB_ID, internetMentions.getValue3());
        }

        map.put(POIConfig.Fields.COORDINATES, coords);
        return map;
    }

    @Override
    public IRI id() {
        return id;
    }
}