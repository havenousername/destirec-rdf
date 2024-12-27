package org.destirec.destirec.rdf4j.vocabulary;

import org.eclipse.rdf4j.model.Namespace;

import static org.destirec.destirec.utils.Constants.RDF_NAMESPACE;


public class DESTIREC {
    public static final String NAMESPACE = RDF_NAMESPACE;
    public static final Namespace NS = new ExternalNamespace("destirec", NAMESPACE);
}
