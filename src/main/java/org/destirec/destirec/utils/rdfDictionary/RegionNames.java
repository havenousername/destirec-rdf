package org.destirec.destirec.utils.rdfDictionary;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public final class RegionNames {

    private RegionNames() {}
    public final static class Classes {

        public final static DESTIREC.NamespaceWrapper PARENT_REGION = DESTIREC.wrap("ParentRegion");
        public final static DESTIREC.NamespaceWrapper LEAF_REGION = DESTIREC.wrap("LeafRegion");
        public final static DESTIREC.NamespaceWrapper REGION = DESTIREC.wrap("Region");
        public final static DESTIREC.NamespaceWrapper ROOT_REGION = DESTIREC.wrap("RootRegion");

    }

    public final static class Properties {
        public final static DESTIREC.NamespaceWrapper HAS_COST = DESTIREC.wrap("hasCost");

        public final static DESTIREC.NamespaceWrapper HAS_MONTH = DESTIREC.wrap("hasMonth");

        public final static DESTIREC.NamespaceWrapper HAS_FEATURE = DESTIREC.wrap("hasFeature");
    }
}
