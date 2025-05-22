package org.destirec.destirec.utils.rdfDictionary;

import lombok.Getter;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public final class QualityNames {
    private QualityNames() {}
    public final static class Classes {
        public final static DESTIREC.NamespaceWrapper QUALITY = DESTIREC.wrap("Quality");
    }

    public final static class Properties {
        public final static DESTIREC.NamespaceWrapper LOWER = DESTIREC.wrap("lower");
        public final static DESTIREC.NamespaceWrapper UPPER = DESTIREC.wrap("upper");
    }

    public final static class Individuals {

        @Getter
        public enum Quality {
            BAD("Bad", -1, 1),
            POOR("Poor", 1, 25),
            FAIR("Fair", 25, 50),
            AVERAGE("Average", 50, 75),
            GOOD("Good", 75, 101);
            private final String name;
            private final int lower;
            private final int upper;

            Quality(String name, int lower, int upper) {
                this.name = name;
                this.lower = lower;
                this.upper = upper;
            }

            public DESTIREC.NamespaceWrapper iri() {
                return DESTIREC.wrap(name);
            }
        }
    }
}
