package org.destirec.destirec.utils.rdfDictionary;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public final class RegionNames {

    private RegionNames() {}
    public final static class Classes {

        public final static String PARENT_REGION = DESTIREC.wrapNamespace("ParentRegion");
        public final static String LEAF_REGION = DESTIREC.wrapNamespace("LeafRegion");
        public final static String REGION = DESTIREC.wrapNamespace("Region");
        public final static String ROOT_REGION = DESTIREC.wrapNamespace("RootRegion");

    }

    public final static class Properties {
        public final static String HAS_COST = DESTIREC.wrapNamespace("hasCost");

        public final static String HAS_MONTHS =DESTIREC.wrapNamespace("hasMonths");

        public final static String HAS_FEATURES =DESTIREC.wrapNamespace("hasMonths");
    }
}
