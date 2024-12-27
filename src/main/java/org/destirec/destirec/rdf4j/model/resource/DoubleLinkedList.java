package org.destirec.destirec.rdf4j.model.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.destirec.destirec.rdf4j.model.predicates.DomainPredicate;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DoubleLinkedList extends Resource<DoubleLinkedList.Fields> {
    private final DomainPredicate previousPredicate;

    private final DomainPredicate nextPredicate;

    private final DomainPredicate currentPredicate;

    public DoubleLinkedList() {
        super( "DoubleLinkedList",
                new HashMap<>(),
                Map.ofEntries(
                        Map.entry(Fields.NEXT, CoreDatatype.XSD.ANYURI),
                        Map.entry(Fields.PREVIOUS, CoreDatatype.XSD.ANYURI),
                        Map.entry(Fields.CURRENT, CoreDatatype.XSD.ANYURI)
                ));

        previousPredicate = new DomainPredicate("previous", RDF.REST, get());
        previousPredicate.setLabel("previous");
        previousPredicate.setComment("Points to the previous node in the double linked list.");


        nextPredicate = new DomainPredicate("next", RDF.REST, get());
        nextPredicate.setLabel("next");
        nextPredicate.setComment("Points to the next node in the double linked list.");


        currentPredicate = new DomainPredicate("current", RDF.VALUE, get());
        currentPredicate.setLabel("current");
        currentPredicate.setComment("Points to the current node");

        fields.put(Fields.NEXT, nextPredicate.get());
        fields.put(Fields.CURRENT, currentPredicate.get());
        fields.put(Fields.PREVIOUS, previousPredicate.get());
    }

    @Getter
    @AllArgsConstructor
    public enum Fields {
        NEXT("next"),
        PREVIOUS("previous"),
        CURRENT("current");

        private final String displayName;
    }

    @Override
    public void setup(ModelBuilder builder, String graphName) {
        builder
                .namedGraph(graphName)
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, RDF.REST)
                .add(get(), RDFS.COMMENT, "A double linked list RDF implementation");
        previousPredicate.setup(builder, graphName);
        currentPredicate.setup(builder, graphName);
        nextPredicate.setup(builder, graphName);
    }

    @Override
    public String getResourceLocation() {
        return null;
    }
}
