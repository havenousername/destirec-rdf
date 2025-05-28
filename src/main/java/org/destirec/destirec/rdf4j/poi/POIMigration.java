package org.destirec.destirec.rdf4j.poi;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.AppOntology;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.AttributeNames;
import org.destirec.destirec.utils.rdfDictionary.POINames;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.springframework.stereotype.Component;


@Component
@Getter
public class POIMigration extends IriMigration implements OntologyDefiner {
    private final AppOntology ontology;

    protected POIMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology ontology) {
        super(rdf4jMethods, POINames.Classes.POI.str());
        this.ontology = ontology;
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, RegionNames.Classes.REGION_LIKE.rdfIri());
    }

    @Override
    public void defineOntology() {
        var ontology = new POIOntology();
        ontology.definePOI();
        ontology.definePOIViaProperties();
    }

    class POIOntology {
        private final OWLClass poi = ontology.getFactory().getOWLClass(POINames.Classes.POI.owlIri());
        private final OWLClass regionLike = ontology.getFactory().getOWLClass(RegionNames.Classes.REGION_LIKE.owlIri());

        public void definePOI() {
            ontology.addAxiom(ontology.getFactory().getOWLSubClassOfAxiom(poi, regionLike));
        }


        public void definePOIViaProperties() {
            OWLObjectProperty sfDirectlyWithin = ontology.getFactory().getOWLObjectProperty(RegionNames.Properties.SF_D_WITHIN);
            OWLClass region = ontology.getFactory().getOWLClass(RegionNames.Classes.REGION.owlIri());
            OWLClassExpression insideOneRegion = ontology.getFactory().getOWLObjectSomeValuesFrom(sfDirectlyWithin, region);
            OWLObjectProperty hasFeature = ontology.getFactory().getOWLObjectProperty(RegionNames.Properties.HAS_FEATURE.owlIri());
            OWLClass feature = ontology.getFactory().getOWLClass(AttributeNames.Classes.FEATURE.owlIri());
            OWLClassExpression hasFeatureConstraint  = ontology.getFactory().getOWLObjectSomeValuesFrom(hasFeature, feature);
            ontology.addAxiom(
                    ontology.getFactory().getOWLEquivalentClassesAxiom(poi,
                    ontology.getFactory().getOWLObjectIntersectionOf(insideOneRegion, hasFeatureConstraint)));
        }
    }
}
