package org.destirec.destirec.rdf4j.interfaces;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfResource;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;

public abstract class BNodeMigration extends Migration {
    protected BNodeMigration(RDF4JTemplate rdf4jMethods, String iriName) {
        super(rdf4jMethods, iriName, RDFResource.B_NODE);
    }

    @Override
    public BNode get() {
        return (BNode) super.get();
    }

    @Override
    public RdfResource getResource() {
        return Rdf.bNode((get()).getID());
    }

    @Override
    protected ModifyQuery handleMigrateQuery(RepositoryConnection connection, TriplePattern[] patterns, Variable subject, Variable object) {
        System.out.println(get());
        connection.remove(get(), null, null);
        connection.remove((Resource) null, null, get());
        return Queries.INSERT()
                            .insert(patterns);
    }
}
