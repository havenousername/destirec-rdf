package org.destirec.destirec.utils.rdfDictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public final class RecommendationNames {
    public static final class Classes {
        public static final DESTIREC.NamespaceWrapper RECOMMENDATION = DESTIREC.wrap("Recommendation");

        public static final DESTIREC.NamespaceWrapper SIMPLE_RECOMMENDATION = DESTIREC.wrap("SimpleRecommendation");

        public static final DESTIREC.NamespaceWrapper BIGGER_THAN_RECOMMENDATION = DESTIREC.wrap("BiggerThanRecommendation");
    }

    public static final class Properties {
        public static final DESTIREC.NamespaceWrapper RECOMMENDED_FOR = DESTIREC.wrap("recommendedFor");
    }


    public static final class Individuals {

        @AllArgsConstructor
        @Getter
        public enum RecommendationStrategies {
            SIMPLE_RECOMMENDATION(Classes.SIMPLE_RECOMMENDATION.str()),
            BIGGER_THAN_RECOMMENDATION(Classes.BIGGER_THAN_RECOMMENDATION.str());

            private final String name;
        }
    }

}
