package org.destirec.destirec.rdf4j.ontology;

import lombok.Getter;
import org.destirec.destirec.rdf4j.vocabulary.DESTIREC;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Value("${app.env.graphdb.use_reasoner}")
    private boolean useReasoner;

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

    public void init() {
        try {
            ontology = manager.createOntology(IRI.create(DESTIREC.NAMESPACE));
            if (useReasoner) {
                syncNewData(ontology);
                rebuildReasoner();
            }

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
//        config.reasonerProgressMonitor= new ConsoleProgressMonitor();
        reasoner = new ReasonerFactory().createReasoner(ontology, config);

        if (!reasoner.isConsistent()) {
            Node<OWLClass> unsatClasses = reasoner.getUnsatisfiableClasses();
            logger.warn("Ontology is inconsistent. Unsatisfiable classes: {}", unsatClasses);
            Set<OWLClass> unsatisfiable = unsatClasses.getEntities();
            if (!unsatisfiable.isEmpty()) {
                logger.warn("Unsatisfiable classes detected:");
            }
//            throw new RuntimeException("Cannot compute inferences");
            // Handle inconsistency appropriately if needed
        } else {
            try {
                reasoner.precomputeInferences(InferenceType.values());
            } catch (ReasonerInterruptedException | TimeOutException e) {
                logger.error("Precomputation failed", e);
                throw new RuntimeException("Failed to precompute inferences", e);
            }
        }
    }

    public void resetABox() {
        OWLDataProperty constantIndProperty = factory.getOWLDataProperty(TopOntologyNames.Properties.CONSTANT_IND.owlIri());
        OWLLiteral booleanTrue = factory.getOWLLiteral(true);
        Set<OWLNamedIndividual> constantIndividuals = ontology.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION, Imports.EXCLUDED)
                .stream()
                .filter(ax -> ax.getProperty().equals(constantIndProperty) && ax.getObject().equals(booleanTrue))
                .map(OWLDataPropertyAssertionAxiom::getSubject)
                .filter(IsAnonymous::isNamed)
                .map(s -> (OWLNamedIndividual)s)
                .collect(Collectors.toSet());


        Set<OWLAxiom> aboxAxioms = ontology.getABoxAxioms(Imports.EXCLUDED);

        // collect all the individuals that have constant individual
        Set<OWLAxiom> axiomsToKeep = new HashSet<>();
        for (OWLNamedIndividual constantIndividual : constantIndividuals) {
            // This gets all axioms where the individual appears as a subject, object, etc.
            Set<OWLIndividualAxiom> axiomsAboutThisIndividual = ontology.getAxioms(constantIndividual, Imports.EXCLUDED);
            axiomsToKeep.addAll(axiomsAboutThisIndividual);
        }

        aboxAxioms.addAll(ontology.getAxioms(AxiomType.CLASS_ASSERTION, Imports.EXCLUDED));
        aboxAxioms.removeAll(axiomsToKeep);

        if (!aboxAxioms.isEmpty()) {
            manager.removeAxioms(ontology, aboxAxioms);
            logger.info("Removed {} ABox axioms from the ontology.", aboxAxioms.size());
        } else {
            logger.info("ABox axioms is empty.");
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
            Model ontologyModel = Rio.parse(new ByteArrayInputStream(output.toByteArray()), "", RDFFormat.TURTLE) ;

            rdf4JMethods.consumeConnection(connection -> {
                try {
                    connection.begin();
                    logger.info("Start ontology transaction for migration");
                    if (!useReasoner) {
                        connection.add(ontologyModel);
                    }

                    connection.commit();
                    logger.info("Ontology migration transaction has successfully finished");
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

    private void addStatementsToOntology(Set<Statement> statements, OWLOntology targetOntology) { // Renamed ontology parameter
        if (statements.isEmpty()) return;
        Set<OWLAxiom> axiomsToAdd = new HashSet<>();
        for (Statement statement : statements) {
            try {
                OWLNamedIndividual subject = factory.getOWLNamedIndividual(statement.getSubject().stringValue());
                IRI predicateIRI = IRI.create(statement.getPredicate().stringValue());

                if (statement.getPredicate().equals(org.eclipse.rdf4j.model.vocabulary.RDF.TYPE) &&
                        statement.getObject() instanceof Resource) {
                    OWLClass objectClass = factory.getOWLClass(statement.getObject().stringValue());
                    axiomsToAdd.add(factory.getOWLClassAssertionAxiom(objectClass, subject));
                } else if (statement.getObject() instanceof Resource) {
                    OWLObjectProperty predicate = factory.getOWLObjectProperty(predicateIRI);
                    OWLNamedIndividual object = factory.getOWLNamedIndividual(statement.getObject().stringValue());
                    axiomsToAdd.add(factory.getOWLObjectPropertyAssertionAxiom(predicate, subject, object));
                } else if (statement.getObject() instanceof Literal rdfLiteral) {
                    OWLDataProperty predicate = factory.getOWLDataProperty(predicateIRI);
                    OWLLiteral owlLiteral;
                    if (rdfLiteral.getDatatype() != null) {
                        OWLDatatype datatype = factory.getOWLDatatype(IRI.create(rdfLiteral.getDatatype().stringValue()));
                        owlLiteral = factory.getOWLLiteral(rdfLiteral.getLabel(), datatype);
                    } else if (rdfLiteral.getLanguage().isPresent()) {
                        owlLiteral = factory.getOWLLiteral(rdfLiteral.getLabel(), rdfLiteral.getLanguage().get());
                    } else {
                        owlLiteral = factory.getOWLLiteral(rdfLiteral.getLabel());
                    }
                    axiomsToAdd.add(factory.getOWLDataPropertyAssertionAxiom(predicate, subject, owlLiteral));
                }
            } catch (IllegalArgumentException e) { // Catch issues with bad IRIs etc.
                logger.warn("Skipping statement due to invalid IRI or entity creation: {}. Error: {}", statement, e.getMessage());
            }
        }
        if (!axiomsToAdd.isEmpty()) {
            manager.addAxioms(targetOntology, axiomsToAdd);
            logger.debug("Added {} axioms to ontology {}.", axiomsToAdd.size(), targetOntology.getOntologyID());
        }
    }

    private Set<Statement> getNewStatements(RepositoryConnection connection) {
        Set<Statement> statements = new HashSet<>();
        String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";
        try (GraphQueryResult result = connection.prepareGraphQuery(QueryLanguage.SPARQL, query).evaluate()) {
            while (result.hasNext()) {
                statements.add(result.next());
            }
        }
        return statements;
    }

    private void syncNewData(OWLOntology ontology) {
        rdf4JMethods.consumeConnection(connection -> {
                Set<Statement> newStatements = getNewStatements(connection);
                if (!newStatements.isEmpty()) {
                    addStatementsToOntology(newStatements, ontology);
                    logger.info("New data synchronized and reasoner rebuilt.");
                } else {
                    logger.info("No new data to synchronize.");
                }
                logger.info("Ontology size: " + ontology.getAxiomCount());
        });
    }

    private void clearInferredGraphInRepository() {
        String inferredGraphUri = TopOntologyNames.Graph.INFERRED.pseudoUri();
        logger.info("Clearing inferred triples graph: <{}>", inferredGraphUri);
        rdf4JMethods.consumeConnection(connection -> {
            try {
                connection.begin();
                connection.prepareUpdate(QueryLanguage.SPARQL,
                        "CLEAR SILENT GRAPH <" + inferredGraphUri + ">").execute();
                connection.commit();
                logger.info("Successfully cleared graph: <{}>", inferredGraphUri);
            } catch (RepositoryException e) {
                if (connection.isActive()) connection.rollback();
                logger.error("Failed to clear graph <{}>: {}", inferredGraphUri, e.getMessage(), e);
            }
        });
    }

    public void triggerFullInference() {
        logger.info("Starting full inference at {}", Instant.now());
        isMigrated = true;
        rdf4JMethods.consumeConnection(connection -> {
            try {
                connection.begin();
                logger.debug("Removing old inferred triples from repository");

                OWLOntology infOntology = manager.createOntology();

                Node<OWLClass> unsatClasses = reasoner.getUnsatisfiableClasses();
                Set<OWLClass> unsatisfiable = unsatClasses.getEntitiesMinusBottom();
                if (!unsatisfiable.isEmpty()) {
                    logger.warn("Unsatisfiable classes detected:");
                    for (OWLClass cls : unsatisfiable) {
                        logger.warn(" - {}", cls);
                    }
                }

                InferredOntologyGenerator generator = getInferredOntologyGenerator();
                generator.fillOntology(manager.getOWLDataFactory(), infOntology);

                logger.debug("Serializing inferred ontology to Turtle format");
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                manager.saveOntology(infOntology, new TurtleDocumentFormat(), outputStream);
                String inferredOntologyString = outputStream.toString();
                logger.debug("Adding inferred triples to repository");
                Model ontologyModel = Rio.parse(new ByteArrayInputStream(outputStream.toByteArray()), "", RDFFormat.TURTLE) ;
                connection.add(ontologyModel, TopOntologyNames.Graph.INFERRED.rdfIri());
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
                        new InferredEquivalentObjectPropertyAxiomGenerator(),
                        new InferredEquivalentClassAxiomGenerator(),         // Equivalent classes (owl:equivalentClass)
                        new InferredDisjointClassesAxiomGenerator(),         // Disjoint classes (owl:disjointWith)
                        new InferredInverseObjectPropertiesAxiomGenerator(), // Inverse properties (owl:inverseOf)
                        new InferredObjectPropertyCharacteristicAxiomGenerator(), // Property characteristics (e.g., transitive)
                        new InferredSubObjectPropertyAxiomGenerator(),
                        new InferredDataPropertyCharacteristicAxiomGenerator(),   // Data property characteristics
                        new InferredSubDataPropertyAxiomGenerator(),         // Subproperty relationships for data properties
                        new InferredPropertyAssertionGenerator()
                )
        );


        return new InferredOntologyGenerator(reasoner, generators);
    }

    @Override
    public void triggerInference() {
        if (!useReasoner) return;
        clearInferredGraphInRepository();
        resetABox();
        syncNewData(ontology);
        rebuildReasoner();
        triggerFullInference();;
    }
}