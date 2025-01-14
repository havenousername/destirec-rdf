package org.destirec.destirec.rdf4j.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.base.InternedIRI;

public class SCHEMA {
    public static final String NAMESPACE = "http://schema.org/";

    public static final Namespace NS = new ExternalNamespace("schema.org", NAMESPACE);

    public static final IRI PRICE_SPECIFICATION = new InternedIRI(NAMESPACE, "PriceSpecification");
}
