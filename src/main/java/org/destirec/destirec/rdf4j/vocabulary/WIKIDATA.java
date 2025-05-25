package org.destirec.destirec.rdf4j.vocabulary;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
            MOUNTAIN("Q8502"),
            LAKE("Q23397"),
            PARK("Q22698"),
            FOREST("Q4421"),
            NATURAL_RESERVE("Q472972"),
            CANYON("Q623578"),

            // ARCHITECTURE
            HISTORIC_DISTRICT("Q839954"),
            UNESCO_SITE("Q839954"),
            CATHEDRAL("Q41176"),
            CASTLE("Q124714"),

            // HIKING
            HIKING_TRAIL("Q209939"),
            CLIMBING_AREA("Q22698"),
            CLIMBING_ROUTE("Q1779811"),
            LOOKOUT_POINT("Q207326"),

            // WINTERSPORTS
            SNOWBOARDING("Q210327"),
            SKIING("Q54202"),
            SKI_JUMPING("Q180809"),
            SLEDDING("Q1506654"),
            SKI_RESORT("Q875538"),
            ICE_CLIMBING("Q173211"),
            SNOW_PARK("Q2202162"),

            // WATERSPORTS
            DIVING_SPOT("Q1337009"),
            SCUBA_DIVING("Q133740"),
            SURF_SPOT("Q652733"),
            MARINA("Q1372364"),
            WATER_PARK("Q37038"),


            // ENTERTAINMENT
            AMUSEMENT_PARK("Q1493709"),
            THEME_PARK("Q1824207"),
            KART_RACING_TRACK("Q1735272"),
            SHOOTING_RANGE("Q1407358"),
            ARCADE("Q133357"),
            ESCAPE_ROOM("Q28154028"),
            FESTIVAL_VENUE("Q183424"),
            ICE_CREAM_SHOP("Q27017155"),
            BOWLING_ALLEY("Q1502956"),
            BEER_GARDEN("Q1324011"),
            BREWERY("Q131734"),


            // CULINARY
            RESTAURANT("Q11707"),
            STREET_FOOD_VENUE("Q18119866"),
            FOOD_MARKET("Q210272"),


            // SHOPPING
            SHOPPING_MALL("Q55488"),
            SOUVENIR_SHOP("Q18534524"),
            MARKET("Q3305213"),

            // BEACH
            BEACH("Q40080"),

            // CULTURE
            MUSEUM("Q33506"),
            ART_GALLERY("Q871905");

            private final String type;
        }
    }



    private WIKIDATA() {}
}
