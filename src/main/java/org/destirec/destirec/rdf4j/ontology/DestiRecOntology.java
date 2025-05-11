package org.destirec.destirec.rdf4j.ontology;

import lombok.Getter;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

@Getter
@Component
public class DestiRecOntology {
    private final OWLOntologyManager manager;
    private final OWLDataFactory factory;
    private OWLOntology ontology;

    private final RDF4JTemplate rdf4JMethods;
    private boolean isMigrated;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    public void loadIntoOntology(String iri) throws OWLOntologyCreationException {
        manager.loadOntologyFromOntologyDocument(IRI.create(iri));
        manager.applyChange(new AddImport(
                ontology, factory.getOWLImportsDeclaration(IRI.create(iri))
        ));
    }

    public DestiRecOntology(RDF4JTemplate rdf4JMethods) {
        this.rdf4JMethods = rdf4JMethods;
        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();
        try {
            ontology = manager.createOntology(IRI.create(DESTIREC.NAMESPACE));
        } catch (OWLOntologyCreationException exception) {
            System.out.println("Cannot create ontology on the iri " + DESTIREC.NAMESPACE);
        }
    }

    public void resetOntology() {
        Set<OWLAxiom> axioms = ontology.getAxioms();
        manager.removeAxioms(ontology, axioms);
        isMigrated = false;
    }

    public void migrate() {
        if (isMigrated) {
            return;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            getManager()
                    .saveOntology(getOntology(), new TurtleDocumentFormat(), output);

            String modelString = output.toString();
            Model ontologyModel = Rio.parse(new ByteArrayInputStream(output.toByteArray()), "", RDFFormat.TURTLE) ;

            rdf4JMethods.consumeConnection(connection -> {
                try {
                    connection.begin();
                    logger.info("Start ontology transaction");
                    ontologyModel.forEach(statement -> {
                        var object = statement.getObject();
                        var subject = statement.getSubject();
                        var predicate = statement.getPredicate();

                        if (subject.isBNode() || object.isBNode() || predicate.isBNode()) {
                            connection.remove(subject, predicate, object);
                            connection.add(statement);
                        } else {
                            TriplePattern pattern = GraphPatterns.tp(
                                    subject,
                                    predicate,
                                    object
                            );

                            ModifyQuery query = Queries.INSERT()
                                    .delete(pattern)
                                    .insert(pattern);

                            logger.debug("Query for update: \n" + query.getQueryString());
                            connection.prepareUpdate(query.getQueryString()).execute();
                        }

                    });
                    connection.commit();

                    isMigrated = true;
//                    connection.begin();
//                    connection.add(ontologyModel);
//                    connection.commit();
//                    isMigrated = true;

                    logger.info("Ontology transaction has successfully finished");

                }  catch (RepositoryException e) {
                    System.out.println("IO update of ontology transaction has failed. Error: " + e.getLocalizedMessage());
                }
            });
        } catch (OWLOntologyStorageException e) {
            System.out.println("Cannot migrate owl ontology. Error: " + e.getLocalizedMessage());
        } catch (IOException e) {
            System.out.println("RIO parsing failed. Error: " + e.getLocalizedMessage());
        }
    }
}
