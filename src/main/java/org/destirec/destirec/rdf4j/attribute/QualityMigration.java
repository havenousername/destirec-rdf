package org.destirec.destirec.rdf4j.attribute;

import lombok.Getter;
import lombok.Setter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.region.RegionDao;
import org.destirec.destirec.utils.rdfDictionary.QualityNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

@Getter
@Component
public class QualityMigration extends IriMigration implements OntologyDefiner {
    @Setter
    private DestiRecOntology destirecOntology;
    private final RegionDao regionDao;
    public QualityMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology ontology, RegionDao regionDao) {
        super(rdf4jMethods, QualityNames.Classes.QUALITY.str());
        this.destirecOntology = ontology;
        this.regionDao = regionDao;
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, TopOntologyNames.Classes.CONCEPT.rdfIri());
    }

    @Override
    public void defineOntology() {
        QualityOntology ontology = new QualityOntology(
                destirecOntology,
                destirecOntology.getFactory(),
                regionDao
        );
        ontology.defineQuality();
        ontology.defineHasQuality();
        ontology.defineRegionsQualities();
    }
}
