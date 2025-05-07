package org.destirec.destirec.rdf4j.vocabulary;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;


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
            return RESOURCE_NS + "/" +  str;
        } else if (type == UriType.B_NODE) {
            return RESOURCE_NS + "/_" + str;
        }
        return ONTOLOGY_NS + "/" + str;
    }

    public record NamespaceWrapper(String pseudoUri, String str) {
        public IRI rdfIri() {
            return SimpleValueFactory.getInstance().createIRI(pseudoUri);
        }

        public org.semanticweb.owlapi.model.IRI owlIri(){
            return org.semanticweb.owlapi.model.IRI.create(pseudoUri);
        }
    }

    public static String wrapNamespace(String str) {
        return wrapNamespace(str, UriType.ONTOLOGY);
    }

    public static NamespaceWrapper wrap(String str) {
        return new NamespaceWrapper(wrapNamespace(str, UriType.ONTOLOGY), str);
    }

    public static String wrapResource(String str) {
        return DESTIREC.RESOURCE_NS + "/" + str;
    }
}
