package org.destirec.destirec.utils;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;

public class Constants {
    private Constants() {}

    // RDF configuration specifics
    public static String RDF_NAMESPACE = "http://destirec.com/";

    public static int RDF_VERSION = 1;

    public static String DEFAULT_GRAPH = DESTIREC.NAMESPACE + ":defaultGraph";
}
