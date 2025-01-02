package org.destirec.destirec.rdf4j.interfaces;

import lombok.Getter;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.ModelClause;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class Migration implements Predicate {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected final RDF4JTemplate rdf4jMethods;

    protected final IRI iri;
    @Getter
    protected ValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Getter
    protected String graphName;


    @Getter
    protected List<Namespace> namespaces;

    protected ModelBuilder builder;

    @Getter
    protected boolean isSetup;

    @Getter
    protected boolean isMigrated;

    protected Migration(RDF4JTemplate rdf4jMethods, String iriName) {
        this.rdf4jMethods = rdf4jMethods;
        this.iri = createMigrationIRI(iriName);

        builder = new ModelBuilder();
        namespaces = new ArrayList<>();
        isSetup = false;
        isMigrated = false;
    }

    private IRI createMigrationIRI(String name) {
        try {
            var iri = valueFactory.createIRI(DESTIREC.NAMESPACE, name);
            logger.info("Created iri " + iri + " for: " + name);
            return iri;
        } catch (Exception e) {
            logger.error("Cannot create adequate iri value");
            throw e;
        }
    }

    @Override
    public IRI get() {
        return iri;
    }

    protected abstract void setupProperties();

    @Override
    public void setup() {
        if (graphName == null) {
            builder.defaultGraph();
        } else {
            builder.namedGraph(graphName);
        }

        setupProperties();

        isSetup = true;
        logger.info("Setup is complete. Model created:\n" + builder.build().toString());
    }

    @Override
    public void migrate() {
        rdf4jMethods.consumeConnection(repositoryConnection -> {;
            try {
                logger.info("[Thread: {}] Transaction for the migration started", Thread.currentThread().getName());

                System.out.println(repositoryConnection);
                repositoryConnection.begin();

                Model model = builder.build();
                ModelClause clause = new ModelClause();
                clause.setModel(model);

                TriplePattern[] patterns = clause.generateInsertPatterns();


                Variable subject = SparqlBuilder.var("s");
                Variable object = SparqlBuilder.var("o");

                ModifyQuery query = Queries.INSERT()
                        .delete(subject.has(get(), object))
                        .insert(patterns);

                logger.info("Query for update: \n" + query.getQueryString());
                repositoryConnection.prepareUpdate(query.getQueryString()).execute();
                repositoryConnection.commit();
                isMigrated = true;
                logger.info("Transaction for the migration finished successfully");
            } catch (Exception e) {
                repositoryConnection.rollback();
                throw new RuntimeException("Migration failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void setNamespaces(List<Namespace> namespaces) {
        this.namespaces = namespaces;

        builder = new ModelBuilder();
        namespaces.forEach(namespace -> builder.setNamespace(namespace));

        if (isSetup) {
            setup();
        }
    }

    @Override
    public void setGraphName(String name) {
        graphName = name;
        if (isSetup) {
            setup();
        }
    }

    @Override
    public void setupAndMigrate() {
        setup();
        migrate();
    }
}
