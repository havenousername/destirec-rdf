package org.destirec.destirec.rdf4j.ontology;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.IriMigrationInstance;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.destirec.destirec.utils.rdfDictionary.TopOntologyNames.Classes.*;

@Component
@Getter
public class TopOntologyMigration extends IriMigration {
    private IriMigrationInstance objectClass;
    private IriMigrationInstance conceptClass;
    private IriMigrationInstance actorClass;
    private IriMigrationInstance eventClass;
    private IriMigrationInstance configClass;
    private ListMigration listMigration;

    protected TopOntologyMigration(RDF4JTemplate rdf4jMethods) {
        super(rdf4jMethods, TOP_ONTOLOGY.str());

        setupClasses();
        setupDisjointness();
    }

    private void setupClasses() {
        objectClass = new IriMigrationInstance(rdf4jMethods, OBJECT.str(), (instance) -> {
            instance.builder()
                    .add(instance.predicate(), RDF.TYPE, OWL.CLASS);
        });

        conceptClass = new IriMigrationInstance(rdf4jMethods, CONCEPT.str(), (instance) -> {
            instance.builder()
                    .add(instance.predicate(), RDF.TYPE, OWL.CLASS);
        });

        actorClass = new IriMigrationInstance(rdf4jMethods, ACTOR.str(), (instance) -> {
            instance.builder()
                    .add(instance.predicate(), RDF.TYPE, OWL.CLASS);
        });

        eventClass = new IriMigrationInstance(rdf4jMethods, EVENT.str(), (instance) -> {
            instance.builder()
                    .add(instance.predicate(), RDF.TYPE, OWL.CLASS);
        });

        configClass = new IriMigrationInstance(rdf4jMethods, CONFIG.str(), (instance) -> {
            instance.builder()
                    .add(instance.predicate(), RDF.TYPE, OWL.CLASS);
        });
    }


    private void setupDisjointness() {
        List<IriMigrationInstance> listOfClasses = List.of(objectClass, conceptClass, actorClass, eventClass, configClass);
        listMigration = new ListMigration(rdf4jMethods, TOP_ONTOLOGY_LIST.str(),
                listOfClasses.stream().map(IriMigration::get).toList());
    }


    @Override
    protected void setupProperties() {}

    @Override
    public void setup() {
        super.setup();
        objectClass.setup();
        conceptClass.setup();
        actorClass.setup();
        eventClass.setup();
        configClass.setup();
        listMigration.setup();

        BNode emptyDisjointNode = valueFactory.createBNode();
        builder
                .subject(emptyDisjointNode)
                .add(RDF.TYPE, OWL.ALLDISJOINTCLASSES)
                .add(OWL.MEMBERS, listMigration.getFirst());

    }

    @Override
    public void migrate() {
        super.migrate();
        objectClass.migrate();
        conceptClass.migrate();
        actorClass.migrate();
        eventClass.migrate();
        configClass.migrate();
        listMigration.migrate();
    }
}
