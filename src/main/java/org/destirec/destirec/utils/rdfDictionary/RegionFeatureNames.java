package org.destirec.destirec.utils.rdfDictionary;

import lombok.Getter;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.aggregates.*;

import java.util.Arrays;
import java.util.function.Function;

public class RegionFeatureNames {
    private RegionFeatureNames() {}
    public final static class Classes {
        public final static DESTIREC.NamespaceWrapper REGION_FEATURE = DESTIREC.wrap("RegionFeature");
    }

    public final static class Individuals {
        public enum RegionFeature {
            NATURE("Nature"),
            SAFETY("Safety", new AggregateMin()),
            ARCHITECTURE("Architecture", new AggregateTopK(10)),
            HIKING("Hiking", new AggregateCount()),
            WINTERSPORTS("Wintersports", new AggregateMax()),
            WATERSPORTS("Watersports", new AggregateMax()),
            BEACH("Beach", new AggregateTopK(5)),
            CULTURE("Culture"),
            CULINARY("Culinary", new AggregateLog()),
            ENTERTAINMENT("Entertainment", new AggregateTopK(10)),
            SHOPPING("Shopping", new AggregateCount());

            @Getter
            private final String name;
            @Getter
            private final Function<String, Double> scoreFunction;

            RegionFeature(String iriLocalName) {
                this.name = iriLocalName;
                scoreFunction = new AggregateMean();
            }

            RegionFeature(String iriLocalName, Function<String, Double> scoreFunction) {
                this.name = iriLocalName;
                this.scoreFunction = scoreFunction;
            }

            public DESTIREC.NamespaceWrapper iri() {
                return DESTIREC.wrap(name);
            }

            public static RegionFeature fromIri(String value) {
                return fromString(Arrays.stream(value.split("/")).toList().getLast());
            }

            public static RegionFeature fromString(String value) {
                for (RegionFeature status : RegionFeature.values()) {
                    if (status.name().equalsIgnoreCase(value)) {
                        return status;
                    }
                }
                throw new IllegalArgumentException("No enum constant for value: " + value);
            }
        }

    }

}
