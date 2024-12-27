package org.destirec.destirec.rdf4j.model.predicates;

import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.rdf4j.vocabulary.WIKIDATA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

public class PreferencePredicate implements Predicate {
    private final IRI iri;

    public PreferencePredicate(String name) {
        iri = SimpleValueFactory.getInstance().createIRI(DESTIREC.NAMESPACE, name);
    }

    @Override
    public IRI get() {
        return iri;
    }

    @Override
    public void setup(ModelBuilder builder, String graphName) {
        builder
                .namedGraph(graphName)
                .add(get(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(get(), RDFS.SUBPROPERTYOF, WIKIDATA.PREFERENCE)
                .add(get(), RDFS.RANGE, RDFS.RESOURCE);
    }

}
