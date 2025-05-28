package org.destirec.destirec.rdf4j.ontology;

import org.destirec.destirec.rdf4j.interfaces.BNodeMigration;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;

import java.util.List;

public class ListMigration extends BNodeMigration {
    protected List<IRI> list;
    protected List<BNode> bNodes;

    protected ListMigration(RDF4JTemplate rdf4jMethods, String iriName, List<IRI> classes) {
        super(rdf4jMethods, iriName);

        this.list = classes;
        bNodes = classes.stream().map(instance -> valueFactory
                        .createBNode(instance.getLocalName()))
                .toList();
    }

    @Override
    protected void setupProperties() {
        for (int i = 0; i < bNodes.size(); i++) {
            Resource next = (i == bNodes.size() - 1) ? RDF.NIL : bNodes.get(i+1);
            builder.subject(bNodes.get(i))
                    .add(RDF.FIRST,  list.get(i))
                    .add(RDF.REST, next);
        }
    }

    public BNode getFirst() {
        return bNodes.getFirst();
    }
}
