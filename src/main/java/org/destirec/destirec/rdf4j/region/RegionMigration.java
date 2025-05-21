package org.destirec.destirec.rdf4j.region;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.IriMigrationInstance;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

@Component
@Getter
public class RegionMigration extends IriMigration implements OntologyDefiner {
    private final DestiRecOntology destiRecOntology;
    private IriMigrationInstance leafPredicate;
    private IriMigrationInstance leafInstance;

    protected RegionMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology destiRecOntology) {
        super(rdf4jMethods, RegionNames.Classes.REGION.str());
        this.destiRecOntology = destiRecOntology;
        initLeafInstance();
        initLeafPredicate();
    }

    protected void initLeafPredicate() {
        leafPredicate = new IriMigrationInstance(
                rdf4jMethods, RegionNames.Properties.CONTAINS_EMPTY.str(),
                instance -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                        .add(instance.predicate(), RDFS.DOMAIN, get())
                        .add(instance.predicate(), RDFS.RANGE, RegionNames.Individuals.NO_REGION)
        );
    }

    protected void initLeafInstance() {
        leafInstance = new IriMigrationInstance(
                rdf4jMethods, RegionNames.Individuals.NO_REGION.str(),
                instance -> instance.builder()
                        .add(instance.predicate(), RDF.TYPE, RegionNames.Classes.NO_REGION.rdfIri())
                        .add(instance.predicate(), TopOntologyNames.Properties.CONSTANT_IND.rdfIri(), valueFactory.createLiteral(true))
        );
    }

    @Override
    public void defineOntology() {
        RegionClassOntology ontology = new RegionClassOntology(
                destiRecOntology.getFactory(),
                destiRecOntology.getManager(),
                getDestiRecOntology().getOntology()
        );
        ontology.defineRegion();
        ontology.defineEmptyRegion();
//        ontology.defineInsideRegion();
//        ontology.defineContainsRegion();
//        ontology.defineRoot();
        ontology.defineLeafRegion();
        ontology.defineParentRegion();
//        ontology.defineLeafRegion();
//        ontology.defineRegionParentOrLeaf();

        RegionPropertiesOntology propertiesOntology = new RegionPropertiesOntology(
                destiRecOntology.getFactory(),
                destiRecOntology.getManager(),
                getDestiRecOntology().getOntology()
        );
        propertiesOntology.defineSfWithinMapping();
        propertiesOntology.defineSfWithinIrreflexive();
        propertiesOntology.defineSfWithinOpposite();
        propertiesOntology.defineSfWithinTransitive();
        propertiesOntology.defineSfDirectlyWithin();
        propertiesOntology.defineDisjointAsymmetry();
    }

    @Override
    protected void setupProperties() {
        // Region \sqsubseteq Object
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, TopOntologyNames.Classes.OBJECT.rdfIri());

    }

    @Override
    public void setup() {
        super.setup();
        leafInstance.setup();
        leafPredicate.setup();
    }

    @Override
    public void migrate() {
        super.migrate();
        leafInstance.migrate();
        leafPredicate.migrate();
    }
}
