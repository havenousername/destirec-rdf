package org.destirec.destirec.rdf4j.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.base.InternedIRI;


public class WIKIDATA {
    public static final String NAMESPACE = "http://wikidata.org#";
    public static final Namespace NS = new ExternalNamespace("wikidata", NAMESPACE);
    public static final IRI ELECTRONIC_DICTIONARY = new InternedIRI(NAMESPACE, "Q1327461");
    public static final IRI PREFERENCE = new InternedIRI(NAMESPACE, "Q908656");

    public static final IRI PERCENT = new InternedIRI(NAMESPACE, "Q11229");

    public static final IRI MONTH = new InternedIRI(NAMESPACE, "Q5151");

    public static final IRI SOFTWARE_VERSION = new InternedIRI(NAMESPACE, "Q20826013");

    public static final IRI RDF = new InternedIRI(NAMESPACE, "Q54872");

    private WIKIDATA() {}
}
