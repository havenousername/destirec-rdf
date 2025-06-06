package org.destirec.destirec.utils.rdfDictionary;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public final class AttributeNames {
    private AttributeNames() {}

    public final static class Classes {
        public final static DESTIREC.NamespaceWrapper ATTRIBUTES_COLLECTION = DESTIREC.wrap("AttributesCollection");
        public final static DESTIREC.NamespaceWrapper SCORED_ATTRIBUTE = DESTIREC.wrap("ScoredAttribute");
        public final static DESTIREC.NamespaceWrapper REGION_ATTRIBUTE = DESTIREC.wrap("RegionAttribute");

        public final static DESTIREC.NamespaceWrapper PREFERENCE_ATTRIBUTE = DESTIREC.wrap("PreferenceAttribute");

        public final static DESTIREC.NamespaceWrapper ATTRIBUTE = DESTIREC.wrap("Attribute");

        public final static DESTIREC.NamespaceWrapper MONTH = DESTIREC.wrap("Month");

        public final static DESTIREC.NamespaceWrapper COST = DESTIREC.wrap("Cost");

        public final static DESTIREC.NamespaceWrapper FEATURE = DESTIREC.wrap("Feature");

    }

    public final static class Properties {
        public final static DESTIREC.NamespaceWrapper HAS_ATTRIBUTE = DESTIREC.wrap("hasAttribute");
        public final static DESTIREC.NamespaceWrapper HAS_COST = DESTIREC.wrap("hasCost");
        public final static DESTIREC.NamespaceWrapper HAS_MONTH = DESTIREC.wrap("hasMonth");

        public final static DESTIREC.NamespaceWrapper HAS_FEATURE = DESTIREC.wrap("hasFeature");
        public final static DESTIREC.NamespaceWrapper HAS_QUALITY = DESTIREC.wrap("hasQuality");
        public final static DESTIREC.NamespaceWrapper HAS_SCORE = DESTIREC.wrap("hasScore");
        public final static DESTIREC.NamespaceWrapper IS_ACTIVE = DESTIREC.wrap("isActive");

        public final static DESTIREC.NamespaceWrapper NAME = DESTIREC.wrap("name");

        public final static DESTIREC.NamespaceWrapper POSITION = DESTIREC.wrap("position");

        public final static DESTIREC.NamespaceWrapper NEXT = DESTIREC.wrap("next");

        public final static DESTIREC.NamespaceWrapper HAS_SCORE_PER_WEEK = DESTIREC.wrap("hasCostPerWeek");
        public final static DESTIREC.NamespaceWrapper HAS_BUDGET_LEVEL = DESTIREC.wrap("hasBudgetLevel");
        public final static DESTIREC.NamespaceWrapper HAS_REGION_FEATURE = DESTIREC.wrap("hasRegionFeature");
    }
}
