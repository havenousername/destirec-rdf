package org.destirec.destirec.rdf4j.interfaces;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfResource;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;

public abstract class IriMigration extends Migration {
    protected IriMigration(RDF4JTemplate rdf4jMethods, String iriName) {
        super(rdf4jMethods, iriName, RDFResource.URI);
    }

    @Override
    protected ModifyQuery handleMigrateQuery(RepositoryConnection connection, TriplePattern[] patterns, Variable subject, Variable object) {
        return Queries.INSERT()
                            .delete(subject.has(get(), object))
                            .insert(patterns);
    }

    @Override
    public RdfResource getResource() {
        return Rdf.iri(get());
    }

    @Override
    public IRI get() {
        return (IRI)super.get();
    }
}
