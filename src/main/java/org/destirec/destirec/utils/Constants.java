package org.destirec.destirec.utils;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public class Constants {
    private Constants() {}
    public static int RDF_VERSION = 1;
    public static String DEFAULT_GRAPH = DESTIREC.NAMESPACE + ":defaultGraph";

    public static int MAX_RDF_RECURSION = 2500;
}
