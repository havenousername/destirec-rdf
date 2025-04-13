package org.destirec.destirec.rdf4j.vocabulary;

import org.eclipse.rdf4j.model.Namespace;

public class GEO {
    public static final String NAMESPACE = "http://www.opengis.net/ont/geosparql#";

    protected static final String RDF_URL = "http://schemas.opengis.net/geosparql/1.0/geosparql_vocab_all.rdf";

    public static final Namespace NS = new ExternalNamespace("geo", NAMESPACE);
}
