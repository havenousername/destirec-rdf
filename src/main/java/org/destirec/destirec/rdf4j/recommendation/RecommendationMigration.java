package org.destirec.destirec.rdf4j.recommendation;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.interfaces.OntologyDefiner;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.utils.rdfDictionary.*;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.semanticweb.owlapi.model.*;
import org.springframework.stereotype.Component;

import static org.destirec.destirec.utils.rdfDictionary.RecommendationNames.Classes.*;


@Component
@Getter
public class RecommendationMigration extends IriMigration implements OntologyDefiner {
    private final DestiRecOntology destiRecOntology;
    public RecommendationMigration(RDF4JTemplate rdf4jMethods, DestiRecOntology destiRecOntology) {
        super(rdf4jMethods, RecommendationNames.Classes.RECOMMENDATION.str());
        this.destiRecOntology = destiRecOntology;
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBPROPERTYOF, TopOntologyNames.Classes.OBJECT.rdfIri())
                .add(get(), RDFS.LABEL, "superclass of all recommendations");

        builder
                .add(SIMPLE_RECOMMENDATION.rdfIri(), RDF.TYPE, OWL.CLASS)
                .add(SIMPLE_RECOMMENDATION.rdfIri(), RDFS.SUBPROPERTYOF, get())
                .add(SIMPLE_RECOMMENDATION.rdfIri(), RDFS.LABEL, "sparql based recommendation");

        builder
                .add(BIGGER_THAN_RECOMMENDATION.rdfIri(), RDF.TYPE, OWL.CLASS)
                .add(BIGGER_THAN_RECOMMENDATION.rdfIri(), RDFS.SUBPROPERTYOF, get())
                .add(BIGGER_THAN_RECOMMENDATION.rdfIri(), RDFS.LABEL, "the most used default version");

        builder
                .add(EXISTS_RECOMMENDATION.rdfIri(), RDF.TYPE, OWL.CLASS)
                .add(EXISTS_RECOMMENDATION.rdfIri(), RDFS.SUBPROPERTYOF, get())
                .add(EXISTS_RECOMMENDATION.rdfIri(), RDFS.LABEL, "exists semantic matching between region x preference");


        builder
                .add(ALL_RECOMMENDATION.rdfIri(), RDF.TYPE, OWL.CLASS)
                .add(ALL_RECOMMENDATION.rdfIri(), RDFS.SUBPROPERTYOF, get())
                .add(ALL_RECOMMENDATION.rdfIri(), RDFS.LABEL, "for all semantic matching between region x preference");
    }

    @Override
    public void defineOntology() {
        var recommendationOntology = new RecommendationOntology();
        recommendationOntology.defineExistsRecommendation();
    }


    class RecommendationOntology {
        private final OWLDataFactory factory = destiRecOntology.getFactory();
        private final OWLClass userWithPreference = factory.getOWLClass(UserNames.Classes.USER_WITH_PREFERENCE.owlIri());
        private final OWLClass regionLike = factory.getOWLClass(RegionNames.Classes.REGION_LIKE.owlIri());
        private final OWLClass quality = factory.getOWLClass(QualityNames.Classes.QUALITY.owlIri());

        private final OWLObjectProperty hasQuality =
                factory.getOWLObjectProperty(AttributeNames.Properties.HAS_QUALITY.owlIri());


        public void defineExistsRecommendation() {
            OWLClassExpression containsQuality = factory.getOWLObjectSomeValuesFrom(hasQuality, quality);

            OWLObjectIntersectionOf regionIntersect = factory.getOWLObjectIntersectionOf(regionLike, containsQuality);
            OWLObjectIntersectionOf userIntersect = factory.getOWLObjectIntersectionOf(userWithPreference, containsQuality);

            OWLObjectIntersectionOf recommendationIntersect = factory.getOWLObjectIntersectionOf(regionIntersect, userIntersect);

            OWLClass existsRecommendation = factory.getOWLClass(EXISTS_RECOMMENDATION.owlIri());
            destiRecOntology.addAxiom(factory.getOWLEquivalentClassesAxiom(existsRecommendation, recommendationIntersect));
        }
    }
}
