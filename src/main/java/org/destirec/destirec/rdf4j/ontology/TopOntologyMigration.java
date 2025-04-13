package org.destirec.destirec.rdf4j.ontology;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.IriMigrationInstance;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class TopOntologyMigration extends IriMigration {
    private IriMigrationInstance objectClass;
    private IriMigrationInstance conceptClass;
    private IriMigrationInstance actorClass;
    private IriMigrationInstance eventClass;
    private ListMigration listMigration;

    protected TopOntologyMigration(RDF4JTemplate rdf4jMethods) {
        super(rdf4jMethods, "TopOntology");

        setupClasses();
        setupDisjointness();
    }

    private void setupClasses() {
        objectClass = new IriMigrationInstance(rdf4jMethods, "Object", (instance) -> {
            instance.builder()
                    .add(instance.predicate(), RDFS.SUBCLASSOF, OWL.CLASS);
        });

        conceptClass = new IriMigrationInstance(rdf4jMethods, "Concept", (instance) -> {
            instance.builder()
                    .add(instance.predicate(), RDFS.SUBCLASSOF, OWL.CLASS);
        });

        actorClass = new IriMigrationInstance(rdf4jMethods, "Actor", (instance) -> {
            instance.builder()
                    .add(instance.predicate(), RDFS.SUBCLASSOF, OWL.CLASS);
        });

        eventClass = new IriMigrationInstance(rdf4jMethods, "Event", (instance) -> {
            instance.builder()
                    .add(instance.predicate(), RDFS.SUBCLASSOF, OWL.CLASS);
        });
    }


    private void setupDisjointness() {
        List<IriMigrationInstance> listOfClasses = List.of(objectClass, conceptClass, actorClass, eventClass);
        listMigration = new ListMigration(rdf4jMethods, "TopOntologyList",
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
        listMigration.migrate();
    }
}
