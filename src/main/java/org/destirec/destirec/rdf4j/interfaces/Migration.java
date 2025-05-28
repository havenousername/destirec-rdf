package org.destirec.destirec.rdf4j.interfaces;

import lombok.Getter;
import lombok.NonNull;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.ModelClause;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfResource;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class Migration implements Predicate {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected final RDF4JTemplate rdf4jMethods;


    @Value("${app.env.graphdb.pie_dir}")
    protected String pieDir;

    protected final Resource iri;
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


    @Getter
    protected boolean isRulesetSet;

    @Getter
    protected RDFResource rdfEntity;

    protected String name;

    protected Migration(RDF4JTemplate rdf4jMethods, String iriName, @NonNull RDFResource entity) {
        this.rdf4jMethods = rdf4jMethods;
        rdfEntity = entity;
        this.iri = createMigrationIRI(iriName);
        this.name = iriName;

        builder = new ModelBuilder();
        namespaces = new ArrayList<>();
        isSetup = false;
        isMigrated = false;
        isRulesetSet = false;
    }

    protected Migration(RDF4JTemplate rdf4jMethods, String iriName) {
        this(rdf4jMethods, iriName, RDFResource.URI);
    }

    protected Resource createMigrationIRI(String name) {
        try {
            Resource iri;
            if (rdfEntity == RDFResource.B_NODE) {
                iri = valueFactory.createBNode(DESTIREC.wrapNamespace(name, DESTIREC.UriType.B_NODE));
            } else  {
                iri = valueFactory.createIRI(DESTIREC.wrapNamespace(name));
            }
            logger.debug("Created iri " + iri + " for: " + name);
            return iri;
        } catch (Exception e) {
            logger.error("Cannot create adequate iri value");
            throw e;
        }
    }

    @Override
    public Resource get() {
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

    protected abstract ModifyQuery handleMigrateQuery(
            RepositoryConnection connection,
            TriplePattern[] patterns,
            Variable subject,
            Variable object
    );

    public abstract RdfResource getResource();

    @Override
    public void migrate() {
        if (isMigrated) {
            return;
        }
        rdf4jMethods.consumeConnection(repositoryConnection -> {;
            try {
                logger.debug("[Thread: {}] Transaction for the migration started", Thread.currentThread().getName());

                repositoryConnection.begin();

                Model model = builder.build();
                ModelClause clause = new ModelClause();
                clause.setModel(model);

                TriplePattern[] patterns = clause.generateInsertPatterns();


                Variable subject = SparqlBuilder.var("s");
                Variable object = SparqlBuilder.var("o");

                ModifyQuery query = handleMigrateQuery(repositoryConnection, patterns, subject, object);

                logger.debug("Query for update: \n" + query.getQueryString());
                repositoryConnection.prepareUpdate(query.getQueryString()).execute();
                repositoryConnection.commit();
                isMigrated = true;
                logger.debug("Transaction for the migration finished successfully");
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
    public void addRuleset() {
        if (isRulesetSet) {
            return;
        }
        Path dir = Paths.get(pieDir + this.name.toLowerCase()  + ".sparql");
        try {
            String ruleset = Files.readString(dir);
            rdf4jMethods.consumeConnection(connection -> {
                logger.debug("Query for ruleset: \n" + ruleset);
                connection.begin();
                Update update = connection.prepareUpdate(QueryLanguage.SPARQL, ruleset);
                update.execute();
                connection.commit();
                isRulesetSet = true;
                logger.debug("Transaction for the migration finished successfully");
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
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
