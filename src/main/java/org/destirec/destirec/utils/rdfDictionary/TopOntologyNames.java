package org.destirec.destirec.utils.rdfDictionary;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public final class TopOntologyNames {

    private TopOntologyNames() {}

    public final static class Classes {
        public final static DESTIREC.NamespaceWrapper TOP_ONTOLOGY  = DESTIREC.wrap("TopOntology");

        public final static DESTIREC.NamespaceWrapper TOP_ONTOLOGY_LIST  = DESTIREC.wrap("TopOntologyList");

        public final static DESTIREC.NamespaceWrapper OBJECT  = DESTIREC.wrap("Object");

        public final static DESTIREC.NamespaceWrapper CONCEPT  = DESTIREC.wrap("Concept");

        public final static DESTIREC.NamespaceWrapper CONFIG  = DESTIREC.wrap("Concept");


        public final static DESTIREC.NamespaceWrapper ACTOR  = DESTIREC.wrap("Actor");

        public final static DESTIREC.NamespaceWrapper EVENT  = DESTIREC.wrap("Event");

        public final static DESTIREC.NamespaceWrapper VERSION = DESTIREC.wrap("SchemaVersion");

    }

    public final static class Graph {
        public final static DESTIREC.NamespaceWrapper INFERRED = DESTIREC.wrap("Inferred", DESTIREC.UriType.GRAPH);
    }

    public final static class Properties {
        public final static DESTIREC.NamespaceWrapper HAS_CONCEPT = DESTIREC.wrap("hasConcept");

        public final static DESTIREC.NamespaceWrapper CONSTANT_IND = DESTIREC.wrap("isConstantIndividual");

        public final static DESTIREC.NamespaceWrapper LAST_MODIFIED = DESTIREC.wrap("lastModified");

        public final static DESTIREC.NamespaceWrapper INFERRED = DESTIREC.wrap("inferred");

        public final static DESTIREC.NamespaceWrapper HAS_SCHEMA_VERSION = DESTIREC.wrap("hasSchemaVersion");
        public final static DESTIREC.NamespaceWrapper HAS_VERSION = DESTIREC.wrap("hasVersion");

    }
}
