package org.destirec.destirec.rdf4j.ontology;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Getter
@Component
public class DestiRecOntology implements AppOntology {
    private final OWLOntologyManager manager;
    private final OWLDataFactory factory;
    private OWLOntology ontology;
    private final RDF4JTemplate rdf4JMethods;
    private boolean isMigrated;
    private long lastSyncTime;
    private OWLReasoner reasoner;
    private final Logger logger = LoggerFactory.getLogger(getClass());


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
        lastSyncTime = System.currentTimeMillis();
    }

    @PostConstruct
    public void init() {
        try {
            ontology = manager.createOntology(IRI.create(DESTIREC.NAMESPACE));
            syncOntologyFromRepository();
            rebuildReasoner();
//            triggerFullInference(); // Perform initial full inference
//            isMigrated = true; // Set migrated to true after initial inference
            logger.info("Ontology initialization completed.");
        } catch (OWLOntologyCreationException exception) {
            logger.error("Cannot create ontology on the IRI {}", DESTIREC.NAMESPACE, exception);
        }
    }

    private void rebuildReasoner() {
        if (reasoner != null) {
            reasoner.dispose();
        }
        Configuration config = new Configuration();
        config.throwInconsistentOntologyException = false;
        config.reasonerProgressMonitor= new ConsoleProgressMonitor();


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            manager.saveOntology(ontology, new TurtleDocumentFormat(), outputStream);
        } catch (OWLOntologyStorageException e) {
            throw new RuntimeException(e);
        }
        reasoner = new ReasonerFactory().createReasoner(ontology, config);

        if (!reasoner.isConsistent()) {
            logger.warn("Ontology is inconsistent. Unsatisfiable classes: {}", reasoner.getUnsatisfiableClasses());
            // Handle inconsistency appropriately if needed
        }
        try {
            reasoner.precomputeInferences(InferenceType.values());
        } catch (ReasonerInterruptedException | TimeOutException e) {
            logger.error("Precomputation failed", e);
            throw new RuntimeException("Failed to precompute inferences", e);
        }
    }

    public void resetOntology() {
        Set<OWLAxiom> axioms = ontology.getAxioms();
        manager.removeAxioms(ontology, axioms);
        isMigrated = false;
        lastSyncTime = System.currentTimeMillis();
        logger.info("Ontology has been reset.");
    }

    public void migrate() {
        if (isMigrated) {
            return;
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            manager.saveOntology(ontology, new TurtleDocumentFormat(), output);
            rdf4JMethods.consumeConnection(connection -> {
                try {
                    connection.begin();
                    logger.info("Start ontology transaction for migration");
                    removeOntologyTriples(connection);
                    connection.commit();
                    logger.info("Ontology migration transaction has successfully finished");

                    // Perform initial full inference after migration
                    triggerInference();
                } catch (RepositoryException e) {
                    connection.rollback();
                    logger.error("IO update of ontology transaction has failed", e);
                }
            });

        } catch (OWLOntologyStorageException e) {
            logger.error("Cannot migrate OWL ontology", e);
        } catch (IOException e) {
            logger.error("RIO parsing failed", e);
        }
    }

    private void addStatementsToOntology(Set<Statement> statements) {
        for (Statement statement : statements) {
            if (statement.getObject() instanceof Resource) {
                OWLNamedIndividual subject = factory.getOWLNamedIndividual(statement.getSubject().stringValue());
                OWLNamedIndividual object = factory.getOWLNamedIndividual(statement.getObject().stringValue());
                OWLObjectProperty predicate = factory.getOWLObjectProperty(statement.getPredicate().stringValue());

                OWLObjectPropertyAssertionAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(predicate, subject, object);
                manager.addAxiom(ontology, axiom);
            } else if (statement.getObject() instanceof Literal) {
                OWLNamedIndividual subject = factory.getOWLNamedIndividual(statement.getSubject().stringValue());
                OWLDataProperty property = factory.getOWLDataProperty(statement.getPredicate().stringValue());
                OWLLiteral literal = factory.getOWLLiteral(((Literal) statement.getObject()).getLabel());
                OWLDataPropertyAssertionAxiom axiom = factory.getOWLDataPropertyAssertionAxiom(property, subject, literal);
                manager.addAxiom(ontology, axiom);
            }
        }
    }

    private void syncOntologyFromRepository() {
        try {
            rdf4JMethods.consumeConnection(connection -> {
                Model model = new LinkedHashModel();
                String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";
                try (GraphQueryResult result = connection.prepareGraphQuery(QueryLanguage.SPARQL, query).evaluate()) {
                    while (result.hasNext()) {
                        model.add(result.next());
                    }
                }
                // Convert RDF4J statements to OWL axioms
                addStatementsToOntology(model);
            });
            logger.info("Ontology synchronized from repository.");
        } catch (Exception e) {
            logger.error("Failed to sync ontology with repository triples", e);
            throw new RuntimeException(e);
        }
    }

    private Set<Statement> getNewStatements(RepositoryConnection connection) {
        Set<Statement> statements = new HashSet<>();
//        String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . OPTIONAL { ?s <" +
//                TopOntologyNames.Properties.LAST_MODIFIED.pseudoUri() +
//                "> ?time } FILTER (!bound(?time) || ?time >  " + lastSyncTime + ") }";

        String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";
        try (GraphQueryResult result = connection.prepareGraphQuery(QueryLanguage.SPARQL, query).evaluate()) {
            while (result.hasNext()) {
                statements.add(result.next());
            }
        }
        return statements;
    }

    private Future<?> syncNewData(ExecutorService executorService) {
        return executorService.submit(() -> {
            rdf4JMethods.consumeConnection(connection -> {
                logger.info("Ontology size: " + ontology.getAxiomCount());
                Set<Statement> newStatements = getNewStatements(connection);
                if (!newStatements.isEmpty()) {
                    addStatementsToOntology(newStatements);
                    logger.info("New data synchronized and reasoner rebuilt.");
                } else {
                    logger.info("No new data to synchronize.");
                }
                rebuildReasoner();
                logger.info("Ontology size: " + ontology.getAxiomCount());
            });
        });
    }

    private void removeOntologyTriples(RepositoryConnection connection) {
        String updateQuery = "DELETE { ?s ?p ?o } WHERE { ?s ?p ?o " +
                "FILTER (STRSTARTS(STR(?s), \"" + DESTIREC.NAMESPACE + "\") || " +
                "STRSTARTS(STR(?p), \"" + DESTIREC.NAMESPACE + "\") || " +
                "STRSTARTS(STR(?o), \"" + DESTIREC.NAMESPACE + "\")) }";
        Update update = connection.prepareUpdate(QueryLanguage.SPARQL, updateQuery);
        update.execute();
    }

    public void updateRepositoryWithInferences() {
        // Deprecated method, kept for backward compatibility
        triggerFullInference();
    }

    public void triggerFullInference() {
        logger.info("Starting full inference at {}", Instant.now());
        isMigrated = true;
        rdf4JMethods.consumeConnection(connection -> {
            try {
                ExecutorService service = Executors.newSingleThreadExecutor();
                connection.begin();
                syncNewData(service).get();
                logger.debug("Removing old inferred triples from repository");
//                removeOntologyTriples(connection);

                OWLOntology infOntology = manager.createOntology();

                Node<OWLClass> unsatClasses = reasoner.getUnsatisfiableClasses();
                Set<OWLClass> unsatisfiable = unsatClasses.getEntitiesMinusBottom();
                if (!unsatisfiable.isEmpty()) {
                    logger.warn("Unsatisfiable classes detected:");
                    for (OWLClass cls : unsatisfiable) {
                        logger.warn(" - {}", cls);

                        BlackBoxExplanation exp= new BlackBoxExplanation(ontology, new ReasonerFactory(), reasoner);
                        HSTExplanationGenerator multExplanator=new HSTExplanationGenerator(exp);
                        // Now we can get explanations for the unsatisfiability.
                        Set<Set<OWLAxiom>> explanations=multExplanator.getExplanations(cls);
                        // Let us print them. Each explanation is one possible set of axioms that cause the
                        // unsatisfiability.
                        for (Set<OWLAxiom> explanation : explanations) {
                            System.out.println("------------------");
                            System.out.println("Axioms causing the unsatisfiability: ");
                            for (OWLAxiom causingAxiom : explanation) {
                                System.out.println(causingAxiom);
                            }
                            System.out.println("------------------");
                        }
                    }
                }

                InferredOntologyGenerator generator = getInferredOntologyGenerator();
                generator.fillOntology(manager.getOWLDataFactory(), infOntology);

                logger.debug("Serializing inferred ontology to Turtle format");
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                manager.saveOntology(infOntology, new TurtleDocumentFormat(), outputStream);
                String inferredOntologyString = outputStream.toString();

                logger.debug("Adding inferred triples to repository");
                connection.add(new ByteArrayInputStream(outputStream.toByteArray()), "", RDFFormat.TURTLE);
                connection.commit();

                lastSyncTime = System.currentTimeMillis();
                logger.info("Full inference completed at {}", Instant.ofEpochMilli(lastSyncTime));

                manager.removeOntology(infOntology);

                ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
                manager.saveOntology(ontology, new TurtleDocumentFormat(), outputStream1);
                String output = outputStream.toString();
                System.out.println("Ontology done");
            } catch (OWLOntologyCreationException e) {
                connection.rollback();
                logger.error("Cannot create new ontology for inference", e);
                throw new RuntimeException(e);
            } catch (OWLOntologyStorageException e) {
                connection.rollback();
                logger.error("Cannot save inferred ontology from manager", e);
                throw new RuntimeException(e);
            } catch (IOException e) {
                connection.rollback();
                logger.error("I/O error while writing inferred triples to the database", e);
                throw new RuntimeException(e);
            } catch (Exception e) {
                connection.rollback();
                logger.error("Unexpected error during full inference", e);
                throw new RuntimeException(e);
            }
        });
    }

    @NotNull
    private InferredOntologyGenerator getInferredOntologyGenerator() {
        List<InferredAxiomGenerator<? extends OWLAxiom>> generators = new ArrayList<>(
                List.of(
                        new InferredClassAssertionAxiomGenerator(),           // Class assertions (rdf:type)
                        new InferredSubClassAxiomGenerator(),                // Subclass relationships (rdfs:subClassOf)
                        new InferredEquivalentClassAxiomGenerator(),         // Equivalent classes (owl:equivalentClass)
                        new InferredDisjointClassesAxiomGenerator(),         // Disjoint classes (owl:disjointWith)
                        new InferredSubObjectPropertyAxiomGenerator(),       // Subproperty relationships (rdfs:subPropertyOf)

                        new InferredInverseObjectPropertiesAxiomGenerator(), // Inverse properties (owl:inverseOf)
                        new InferredObjectPropertyCharacteristicAxiomGenerator(), // Property characteristics (e.g., transitive)
                        new InferredDataPropertyCharacteristicAxiomGenerator(),   // Data property characteristics
                        new InferredSubDataPropertyAxiomGenerator(),         // Subproperty relationships for data properties
                        new InferredPropertyAssertionGenerator() {           // Custom generator for object/data property assertions
                            @Override
                            protected void addAxioms(
                                    @NotNull OWLNamedIndividual entity,
                                    @NotNull OWLReasoner reasoner,
                                    @NotNull OWLDataFactory dataFactory,
                                    @NotNull Set<OWLPropertyAssertionAxiom<?, ?>> result
                            ) {
                                // Object property assertions
                                for (OWLObjectProperty prop : ontology.getObjectPropertiesInSignature()) {
                                    Set<OWLNamedIndividual> values = reasoner.getObjectPropertyValues(entity, prop).getFlattened();
                                    for (OWLNamedIndividual obj : values) {
                                        OWLObjectPropertyAssertionAxiom axiom = dataFactory.getOWLObjectPropertyAssertionAxiom(prop, entity, obj);
                                        result.add(axiom);
                                    }
                                }
                                // Data property assertions
                                for (OWLDataProperty prop : ontology.getDataPropertiesInSignature()) {
                                    Set<OWLLiteral> values = reasoner.getDataPropertyValues(entity, prop);
                                    for (OWLLiteral literal : values) {
                                        OWLDataPropertyAssertionAxiom axiom = dataFactory.getOWLDataPropertyAssertionAxiom(prop, entity, literal);
                                        result.add(axiom);
                                    }
                                }
                            }
                        }
                )
        );


        return new InferredOntologyGenerator(reasoner, generators);
    }

    public void triggerIncrementalInference() {
        logger.info("Starting incremental inference at {}", Instant.now());
        rdf4JMethods.consumeConnection(connection -> {
            try {
                connection.begin();
                logger.debug("Syncing new data");
                OWLOntology infOntology = manager.createOntology();

                // Populating class assertions
//                logger.debug("Populating class assertions for inference");
//                for (OWLClass cls : ontology.getClassesInSignature()) {
//                    Set<OWLNamedIndividual> instances = reasoner.getInstances(cls, false).getFlattened();
//                    for (OWLNamedIndividual ind : instances) {
//                        OWLClassAssertionAxiom ax = factory.getOWLClassAssertionAxiom(cls, ind);
//                        manager.addAxiom(infOntology, ax);
//                    }
//                }

                // Populating object property assertions
                logger.debug("Populating object property assertions for inference");
                for (OWLObjectProperty prop : ontology.getObjectPropertiesInSignature()) {
                    for (OWLNamedIndividual sub : ontology.getIndividualsInSignature()) {
                        Set<OWLNamedIndividual> values = reasoner.getObjectPropertyValues(sub, prop).getFlattened();
                        for (OWLNamedIndividual obj : values) {
                            OWLObjectPropertyAssertionAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(prop, sub, obj);
                            manager.addAxiom(infOntology, ax);
                        }
                    }
                }

                logger.debug("Serializing inferred ontology to Turtle format");
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                manager.saveOntology(infOntology, new TurtleDocumentFormat(), outputStream);

                logger.debug("Adding inferred triples to repository");
                connection.add(new ByteArrayInputStream(outputStream.toByteArray()), "", RDFFormat.TURTLE);
                connection.commit();

                lastSyncTime = System.currentTimeMillis();
                logger.info("Incremental inference completed at {}", Instant.ofEpochMilli(lastSyncTime));

                manager.removeOntology(infOntology);

            } catch (OWLOntologyCreationException e) {
                connection.rollback();
                logger.error("Cannot create new ontology for incremental inference", e);
                throw new RuntimeException(e);
            } catch (OWLOntologyStorageException e) {
                connection.rollback();
                logger.error("Cannot save incremental inferred ontology from manager", e);
                throw new RuntimeException(e);
            } catch (IOException e) {
                connection.rollback();
                logger.error("I/O error while writing incremental inferred triples to the database", e);
                throw new RuntimeException(e);
            } catch (Exception e) {
                connection.rollback();
                logger.error("Unexpected error during incremental inference", e);
                throw new RuntimeException(e);
            }
        });
    }

    public void triggerInference() {
//        if (!isMigrated) {
            triggerFullInference();
//            isMigrated = true;
//        } else {
//            triggerIncrementalInference();
//        }
    }
}