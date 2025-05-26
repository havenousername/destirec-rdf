package org.destirec.destirec.rdf4j.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.base.InternedIRI;

public class DBPEDIA {
    public static final String NAMESPACE = "http://dbpedia.org/ontology/";

    public static final String RDF = "http://dbpedia.org/sparql/";

    public static final Namespace NS = new ExternalNamespace("dbpedia", NAMESPACE);

    public static final IRI COST = new InternedIRI(NAMESPACE, "cost");

    public static final IRI INTEREST = new InternedIRI(NAMESPACE, "interest");

    public static final IRI REGION = new InternedIRI(NAMESPACE, "region");
}
