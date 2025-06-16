package org.destirec.destirec.rdf4j.vocabulary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames.Individuals.RegionFeature;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.base.InternedIRI;

import java.net.URI;


public class WIKIDATA {
    public static final String NAMESPACE = "http://wikidata.org#";

    public static final String PROPERTY = "http://www.wikidata.org/prop/direct/";

    public static final URI SPARQL_ENDPOINT = URI.create("https://query.wikidata.org/sparql");
    public static final URI DICTIONARY_ENDPOINT = URI.create("https://wikiba.se/ontology#");
    public static final String WDT = "http://www.wikidata.org/prop/direct/";
    public static final String WD = "http://www.wikidata.org/entity/";
    public static final Namespace NS = new ExternalNamespace("wikidata", NAMESPACE);
    public static final IRI ELECTRONIC_DICTIONARY = new InternedIRI(WD, "Q1327461");
    public static final IRI PREFERENCE = new InternedIRI(WD, "Q908656");

    public static final IRI PERCENT = new InternedIRI(WD, "Q11229");

    public static final IRI MONTH = new InternedIRI(WD, "Q5151");

    public static final IRI SOFTWARE_VERSION = new InternedIRI(WD, "Q20826013");

    public static final IRI RDF = new InternedIRI(WD, "Q54872");

    public static final IRI WEEK = new InternedIRI(WD, "Q23387");

    // general properties in wikidata

    public static final IRI INSTANCE_OF = new InternedIRI(WDT, "P31");
    public static final IRI PART_OF = new InternedIRI(WDT, "P361");

    public static final class RegionOntology {
        public static final IRI CONTINENT = new InternedIRI(WD,"Q5107");
        public static final IRI EARTH = new InternedIRI(WD,"Q2");


        @AllArgsConstructor
        @Getter
        public enum QTypes {
            // NATURE
            NATURAL_ATTRACTION("Q14226459"),
            MOUNTAIN("Q8502"),
            SUMMIT("Q207326"),
            LAKE("Q23397"),
            PARK("Q22698"),
            FOREST("Q5469146"),
            NATURAL_RESERVE("Q179049"),
            CANYON("Q150784"),
            // BEACH
            BEACH("Q40080"),

            // ARCHITECTURE / HISTORICAL / CULTURAL LANDMARKS
            HISTORIC_DISTRICT("Q15243209"),
            UNESCO_SITE("Q9259"),
            CATHEDRAL("Q2977"),
            CASTLE("Q23413"),
            ARCHITECTURAL_LANDMARK("Q2319498"),
            BUILDING("Q41176"),

            // CULTURE
            MUSEUM("Q33506"),
            ART_GALLERY("Q1007870"),
            OPERA_HOUSE("Q153562"),
            THEATRE_BUILDING("Q24354"),

            // HIKING / CLIMBING
            HIKING_TRAIL("Q2143825"),
            CLIMBING_AREA("Q1640361"),
            CLIMBING_ROUTE("Q1699583"),
            LOOKOUT_POINT("Q6017969"),

            // WINTERSPORTS VENUES/AREAS
            SKI_RESORT("Q130003"),
            SNOW_PARK("Q3141488"),
            SKI_JUMPING_HILL("Q1109069"),

            // WATERSPORTS VENUES
            DIVING_SPOT("Q179643"),
            SURF_SPOT("Q2368508"),
            WATER_PARK("Q740326"),

            // ENTERTAINMENT VENUES & ATTRACTIONS
            AMUSEMENT_PARK("Q194195"),
            THEME_PARK("Q2416723"),
            CIRCUS("Q477396"),
            RACE_TRACK("Q1777138"),
            KARTING_CIRCUIT("Q1232319"),
            SHOOTING_RANGE("Q521839"),
            STADIUM("Q483110"),
            OCEANARIUM("Q1443808"),
            ROLLER_COASTER("Q1265865"),
            FERRIS_WHEEL("Q202570"),
            SKY_COASTER("Q3486441"),
            FLYING_THEATER("Q18326400"),
            MARINE_MAMMAL_PARK("Q15060435"),
            ARCADE_VENUE("Q33097655"),
            ESCAPE_ROOM("Q17015069"),
            FESTIVAL_VENUE("Q183424"),
            FESTIVAL_EVENT("Q132241"),
            BOWLING_ALLEY("Q27106471"),
            BREWERY("Q131734"),
            // CULINARY
            RESTAURANT("Q11707"),
            ICE_CREAM_SHOP("Q1311064"),
            BEER_GARDEN("Q857909"),
            STREET_FOOD_VENUE("Q1316209"),
            FOOD_MARKET("Q1192284"),

            // SHOPPING
            SHOPPING_MALL("Q31374404"),
            SOUVENIR_SHOP("Q865693"),
            MARKET("Q330284"),

            // OTHER
            SPA_TOWN("Q6882870");

            public static QTypes getQTypeFromIRI(String iri) {
                for (QTypes qType : QTypes.values()) {
                    if (iri.contains(qType.getType())) {
                        return qType;
                    }
                }

                throw new IllegalArgumentException("QType cannot be taken from iri " + iri);
            }

            public static RegionFeature getRegionFeature(QTypes qType) {
                return switch (qType) {
                    case MOUNTAIN, SUMMIT, LAKE, PARK, FOREST, NATURAL_RESERVE, CANYON, NATURAL_ATTRACTION ->
                            RegionFeature.NATURE;

                    case HISTORIC_DISTRICT, UNESCO_SITE, CATHEDRAL, CASTLE, ARCHITECTURAL_LANDMARK, BUILDING ->
                            RegionFeature.ARCHITECTURE;

                    case MUSEUM, ART_GALLERY, OPERA_HOUSE, THEATRE_BUILDING -> RegionFeature.CULTURE;

                    case HIKING_TRAIL, CLIMBING_AREA, CLIMBING_ROUTE, LOOKOUT_POINT ->
                            RegionFeature.HIKING;

                    case SKI_RESORT, SNOW_PARK, SKI_JUMPING_HILL ->
                            RegionFeature.WINTERSPORTS;

                    case DIVING_SPOT, SURF_SPOT, WATER_PARK ->
                            RegionFeature.WATERSPORTS;

                    case AMUSEMENT_PARK, THEME_PARK, SHOOTING_RANGE, ESCAPE_ROOM, FESTIVAL_VENUE, ICE_CREAM_SHOP,
                         BOWLING_ALLEY, BEER_GARDEN, CIRCUS, RACE_TRACK, KARTING_CIRCUIT, STADIUM, OCEANARIUM,
                         ROLLER_COASTER,  FERRIS_WHEEL, SKY_COASTER, FLYING_THEATER, MARINE_MAMMAL_PARK, ARCADE_VENUE,
                         FESTIVAL_EVENT, SPA_TOWN, BREWERY   ->
                            RegionFeature.ENTERTAINMENT;

                    case RESTAURANT, STREET_FOOD_VENUE, FOOD_MARKET ->
                            RegionFeature.CULINARY;

                    case SHOPPING_MALL, SOUVENIR_SHOP, MARKET ->
                            RegionFeature.SHOPPING;

                    case BEACH ->
                            RegionFeature.BEACH;
                };

            }

            public DESTIREC.NamespaceWrapper iri() {
                return DESTIREC.wrap(type);
            }

            private final String type;
        }
    }



    private WIKIDATA() {}
}
