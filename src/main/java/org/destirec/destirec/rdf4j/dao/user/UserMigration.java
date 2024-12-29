package org.destirec.destirec.rdf4j.dao.user;

import lombok.Getter;
import lombok.NonNull;
import org.destirec.destirec.rdf4j.dao.interfaces.Predicate;
import org.destirec.destirec.utils.ModelClause;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserMigration implements Predicate {
    private final RDF4JTemplate rdf4jMethods;

    private final IRI userIRI;

    @Getter
    private String graphName;


    @Getter
    private List<Namespace> namespaces;

    private ModelBuilder builder;

    private boolean isSetup;

    public UserMigration(@Autowired RDF4JTemplate template) {
        rdf4jMethods = template;
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        userIRI = valueFactory.createIRI("User");
        builder = new ModelBuilder();
        namespaces = new ArrayList<>();
        isSetup = false;
    }

    @Override
    public IRI get() {
        return userIRI;
    }

    @Override
    public void setup() {
        if (graphName == null) {
            builder.defaultGraph();
        } else {
            builder.namedGraph(graphName);
        }
        builder.add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, FOAF.PERSON)
                .add(get(), RDFS.COMMENT, "A user of an application");
        isSetup = true;
    }

    @Override
    public void migrate() {
      rdf4jMethods.consumeConnection(repositoryConnection -> {
          try {
              repositoryConnection.begin();

              Model model = builder.build();
              ModelClause clause = new ModelClause();
              clause.setModel(model);

              TriplePattern[] patterns = clause.generateInsertPatterns();


              Variable subject = SparqlBuilder.var("s");
              Variable object = SparqlBuilder.var("o");

              ModifyQuery query = Queries.INSERT()
                      .delete(subject.has(get(), object))
                      .insert(patterns)
                      .where(subject.has(get(), object));

              repositoryConnection.prepareUpdate(query.getQueryString()).execute();
              repositoryConnection.commit();
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
    public void setGraphName(@NonNull String graphName) {
        this.graphName = graphName;
        if (isSetup) {
            setup();
        }
    }
}
