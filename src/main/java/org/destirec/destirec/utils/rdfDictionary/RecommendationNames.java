package org.destirec.destirec.utils.rdfDictionary;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public final class RecommendationNames {
    public static final class Classes {
        public static final DESTIREC.NamespaceWrapper RECOMMENDATION = DESTIREC.wrap("Recommendation");

        public static final DESTIREC.NamespaceWrapper SIMPLE_RECOMMENDATION = DESTIREC.wrap("SimpleRecommendation");
    }

    public static final class Properties {
        public static final DESTIREC.NamespaceWrapper RECOMMENDED_FOR = DESTIREC.wrap("recommendedFor");
    }

}
