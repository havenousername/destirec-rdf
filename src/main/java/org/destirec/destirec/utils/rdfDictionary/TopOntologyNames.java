package org.destirec.destirec.utils.rdfDictionary;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public final class TopOntologyNames {

    private TopOntologyNames() {}

    public final static class Classes {
        public final static String TOP_ONTOLOGY  = DESTIREC.wrapNamespace("TopOntology");

        public final static String OBJECT  = DESTIREC.wrapNamespace("Object");

        public final static String CONCEPT  = DESTIREC.wrapNamespace("Concept");


        public final static String ACTOR  = DESTIREC.wrapNamespace("Actor");

        public final static String EVENT  = DESTIREC.wrapNamespace("Event");

    }

    public final static class Properties {
        public final static DESTIREC.NamespaceWrapper HAS_CONCEPT = DESTIREC.wrap("hasConcept");
    }
}
