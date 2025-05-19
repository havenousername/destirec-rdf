package org.destirec.destirec.rdf4j.region;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
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

    protected RegionMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology destiRecOntology) {
        super(rdf4jMethods, RegionNames.Classes.REGION.str());
        this.destiRecOntology = destiRecOntology;
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
}
