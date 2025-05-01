package org.destirec.destirec.rdf4j.vocabulary;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Namespace;


public class DESTIREC {
    public enum UriType {
        ONTOLOGY,
        RESOURCE,

        B_NODE
    }
    public static final String NAMESPACE = "http://destirec.com/";
    public static final Namespace NS = new ExternalNamespace("destirec", NAMESPACE);

    private static final String ONTOLOGY_NS = "http://destirec.com/ontology";

    private static final String RESOURCE_NS = "http://destirec.com/resource";

    public static String wrapNamespace(String str, @NonNull UriType type) {
        if (type == UriType.RESOURCE) {
            return RESOURCE_NS + str;
        } else if (type == UriType.B_NODE) {
            return RESOURCE_NS + "_" + str;
        }
        return ONTOLOGY_NS + str;
    }

    public static String wrapNamespace(String str) {
        return wrapNamespace(str, UriType.ONTOLOGY);
    }
}
