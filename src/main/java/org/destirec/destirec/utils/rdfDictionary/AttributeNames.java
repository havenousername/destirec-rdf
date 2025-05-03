package org.destirec.destirec.utils.rdfDictionary;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public final class AttributeNames {
    private AttributeNames() {}

    public final static class Classes {
        public final static String SCORED_ATTRIBUTE = DESTIREC.wrapNamespace("ScoredAttribute");
        public final static String REGION_ATTRIBUTE = DESTIREC.wrapNamespace("RegionAttribute");

        public final static String PREFERENCE_ATTRIBUTE = DESTIREC.wrapNamespace("RegionAttribute");

        public final static String ATTRIBUTE = DESTIREC.wrapNamespace("Attribute");

        public final static String MONTH = DESTIREC.wrapNamespace("Month");
    }

    public final static class Properties {
        public final static String HAS_SCORE = DESTIREC.wrapNamespace("hasScore");
        public final static String IS_ACTIVE = DESTIREC.wrapNamespace("isActive");

        public final static String HAS_ATTRIBUTE = DESTIREC.wrapNamespace("hasAttribute");

        public final static String NAME = DESTIREC.wrapNamespace("name");

        public final static String POSITION = DESTIREC.wrapNamespace("position");

        public final static String NEXT = DESTIREC.wrapNamespace("next");
    }
}
