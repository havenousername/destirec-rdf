package org.destirec.destirec.rdf4j.attributesCollection;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.IriMigrationInstance;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AttributesCollectionMigration extends IriMigration implements OntologyDefiner {
    private IriMigrationInstance hasCost;
    private IriMigrationInstance hasMonths;
    private IriMigrationInstance hasFeatures;
    private final DestiRecOntology destiRecOntology;

    protected AttributesCollectionMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology destiRecOntology) {
        super(rdf4jMethods, AttributeNames.Classes.ATTRIBUTES_COLLECTION.str());
        this.destiRecOntology = destiRecOntology;
    }

    @PostConstruct
    public void init() {
        initHasCost();
        initHasFeatures();
        initHasMonths();
        defineOntology();
    }


    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, TopOntologyNames.Classes.CONCEPT);
    }

    private void initHasCost() {
        hasCost = new IriMigrationInstance(
                rdf4jMethods, RegionNames.Properties.HAS_COST.str(),
                (instance) -> instance
                        .builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.OBJECTPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.RANGE, AttributeNames.Classes.COST.rdfIri())
                        .add(instance.predicate(), RDFS.LABEL, "connect to cost")
        );
    }


    private void initHasMonths() {
        hasMonths = new IriMigrationInstance(
                rdf4jMethods, RegionNames.Properties.HAS_MONTH.str(),
                (instance) -> instance
                        .builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.OBJECTPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.RANGE, AttributeNames.Classes.MONTH.rdfIri())
                        .add(instance.predicate(), RDFS.LABEL, "connect to region months")
        );
    }

    private void initHasFeatures() {
        hasFeatures = new IriMigrationInstance(
                rdf4jMethods, RegionNames.Properties.HAS_FEATURE.str(),
                (instance) -> instance
                        .builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.OBJECTPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.RANGE, AttributeNames.Classes.FEATURE.rdfIri())
                        .add(instance.predicate(), RDFS.LABEL, "connect to region features")
        );
    }

    @Override
    public void defineOntology() {
        var ontology = new AttributeCollectionOntology();
        ontology.defineAttributes();
    }


    class AttributeCollectionOntology {
        private final OWLDataFactory factory = destiRecOntology.getFactory();
        private final OWLOntologyManager manager = destiRecOntology.getManager();
        private final OWLClass attributesCollection = factory
                .getOWLClass(AttributeNames.Classes.ATTRIBUTES_COLLECTION.owlIri());
        private final OWLOntology ontology = destiRecOntology.getOntology();

        private final OWLClass feature = factory.getOWLClass(AttributeNames.Classes.FEATURE.owlIri());


        public void defineAttributes() {
            OWLClass concept = factory.getOWLClass(TopOntologyNames.Classes.CONCEPT);
            // define for now that only leaf region has cost, months, and features
            OWLObjectProperty hasCost = factory.getOWLObjectProperty(AttributeNames.Properties.HAS_COST.owlIri());
            OWLClassExpression exactOneCost = factory
                    .getOWLObjectExactCardinality(1, hasCost);

            OWLObjectProperty hasMonth = factory.getOWLObjectProperty(AttributeNames.Properties.HAS_MONTH.owlIri());
            OWLClassExpression exact12Months = factory
                    .getOWLObjectExactCardinality(12, hasMonth);


            OWLObjectProperty hasFeature = factory.getOWLObjectProperty(AttributeNames.Properties.HAS_FEATURE.owlIri());
            OWLClassExpression existsFeature =  factory
                    .getOWLObjectSomeValuesFrom(hasFeature, feature);
            OWLClassExpression properties = factory
                    .getOWLObjectIntersectionOf(exact12Months, exactOneCost, existsFeature, concept);

            manager
                    .addAxiom(ontology, factory.getOWLEquivalentClassesAxiom(attributesCollection, properties));
        }
    }

    @Override
    public void setup() {
        super.setup();
        hasCost.setup();
        hasMonths.setup();
        hasFeatures.setup();
    }

    @Override
    public void migrate() {
        super.migrate();
        hasCost.migrate();
        hasMonths.migrate();
        hasFeatures.migrate();
    }
}
