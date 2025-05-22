package org.destirec.destirec.utils.rdfDictionary;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.eclipse.rdf4j.model.vocabulary.GEO;

public final class RegionNames {

    private RegionNames() {}
    public final static class Classes {

        public final static DESTIREC.NamespaceWrapper PARENT_REGION = DESTIREC.wrap("ParentRegion");
        public final static DESTIREC.NamespaceWrapper LEAF_REGION = DESTIREC.wrap("LeafRegion");

        public final static DESTIREC.NamespaceWrapper INSIDE_REGION = DESTIREC.wrap("InsideRegion");
        public final static DESTIREC.NamespaceWrapper CONTAINS_REGION = DESTIREC.wrap("ContainsRegion");
        public final static DESTIREC.NamespaceWrapper REGION = DESTIREC.wrap("Region");

        public final static DESTIREC.NamespaceWrapper REGION_LIKE = DESTIREC.wrap("RegionLike");
        public final static DESTIREC.NamespaceWrapper ROOT_REGION = DESTIREC.wrap("RootRegion");

        public final static DESTIREC.NamespaceWrapper NO_REGION = DESTIREC.wrap("EmptyRegion");

    }

    public final static class Properties {
        public final static String SF_WITHIN = GEO.NAMESPACE + "sfTransitiveWithin";
        public final static String SF_D_WITHIN = GEO.sfWithin.stringValue();

        public final static DESTIREC.NamespaceWrapper CONTAINS_EMPTY = DESTIREC.wrap("containsEmpty");

        public final static String SF_CONTAINS = GEO.NAMESPACE + "sfTransitiveContains";

        public final static DESTIREC.NamespaceWrapper HAS_CHILDREN = DESTIREC.wrap("hasChildren");
        public final static String SF_D_CONTAINS = GEO.sfContains.stringValue();


        public final static DESTIREC.NamespaceWrapper NAME = DESTIREC.wrap("name");

        public final static DESTIREC.NamespaceWrapper FOR_FEATURE = DESTIREC.wrap("forFeature");

        public final static DESTIREC.NamespaceWrapper PARENT_REGION = DESTIREC.wrap("parentRegion");

        public final static DESTIREC.NamespaceWrapper HAS_COST = DESTIREC.wrap("hasCost");

        public final static DESTIREC.NamespaceWrapper HAS_MONTH = DESTIREC.wrap("hasMonth");

        public final static DESTIREC.NamespaceWrapper HAS_FEATURE = DESTIREC.wrap("hasFeature");
    }

    public final static class Individuals {
        public final static DESTIREC.NamespaceWrapper NO_REGION = DESTIREC.wrap("emptyRegion");
    }
}
