package org.destirec.destirec.utils.rdfDictionary;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public class RegionFeatureNames {
    private RegionFeatureNames() {}
    public final static class Classes {
        public final static DESTIREC.NamespaceWrapper REGION_FEATURE = DESTIREC.wrap("RegionFeature");
    }

    public final static class Individuals {
        public enum RegionFeature {
            NATURE("Nature"),
            ARCHITECTURE("Architecture"),
            HIKING("Hiking"),
            WINTERSPORTS("Wintersports"),
            BEACH("Beach"),
            CULTURE("Culture"),
            CULINARY("Culinary"),
            ENTERTAINMENT("Entertainment"),
            SHOPPING("Shopping");

            private final String name;

            RegionFeature(String iriLocalName) {
                this.name = iriLocalName;
            }

            public DESTIREC.NamespaceWrapper iri() {
                return DESTIREC.wrap(name);
            }
        }

    }

}
