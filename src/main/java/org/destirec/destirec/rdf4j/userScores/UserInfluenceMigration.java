package org.destirec.destirec.rdf4j.userScores;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.UserNames;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

@Component
@Getter
public class UserInfluenceMigration extends IriMigration {
    // strength of the new user perception
    public static final double BETA = 0.8;
    protected UserInfluenceMigration(RDF4JTemplate rdf4jMethods) {
        super(rdf4jMethods, UserNames.Classes.USER_INFLUENCE.str());
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, UserNames.Classes.USER_INFLUENCE.rdfIri())
                .add(get(), RDFS.LABEL, "User influence superclass" );

        // setup the activity p_score and c_confidence for the user_influence
        builder
                .add(UserNames.Properties.HAS_INFLUENCE_P_SCORE.rdfIri(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(UserNames.Properties.HAS_INFLUENCE_P_SCORE.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.HAS_INFLUENCE_P_SCORE.rdfIri(), RDFS.RANGE, XSD.DOUBLE) // POI or Region
                .add(UserNames.Properties.HAS_INFLUENCE_P_SCORE.rdfIri(), RDFS.LABEL, "has p score entity")
                .add(get(), OWL.HASKEY, UserNames.Properties.HAS_INFLUENCE_P_SCORE.rdfIri());

        builder
                .add(UserNames.Properties.HAS_INFLUENCE_C_CONFIDENCE.rdfIri(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(UserNames.Properties.HAS_INFLUENCE_C_CONFIDENCE.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.HAS_INFLUENCE_C_CONFIDENCE.rdfIri(), RDFS.RANGE, XSD.DOUBLE) // POI or Region
                .add(UserNames.Properties.HAS_INFLUENCE_C_CONFIDENCE.rdfIri(), RDFS.LABEL, "has confidence score")
                .add(get(), OWL.HASKEY, UserNames.Properties.HAS_INFLUENCE_C_CONFIDENCE.rdfIri());

        builder
                .add(UserNames.Properties.HAS_P_SCORES.rdfIri(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(UserNames.Properties.HAS_P_SCORES.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.HAS_P_SCORES.rdfIri(), RDFS.RANGE, XSD.STRING) // POI or Region
                .add(UserNames.Properties.HAS_P_SCORES.rdfIri(), RDFS.LABEL, "has confidence score")
                .add(get(), OWL.HASKEY, UserNames.Properties.HAS_P_SCORES.rdfIri());

        builder
                .add(UserNames.Properties.HAS_C_CONFIDENCES.rdfIri(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(UserNames.Properties.HAS_C_CONFIDENCES.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.HAS_C_CONFIDENCES.rdfIri(), RDFS.RANGE, XSD.STRING) // POI or Region
                .add(UserNames.Properties.HAS_C_CONFIDENCES.rdfIri(), RDFS.LABEL, "has confidence score")
                .add(get(), OWL.HASKEY, UserNames.Properties.HAS_C_CONFIDENCES.rdfIri());

        builder
                .add(UserNames.Properties.INFLUENCE_FOR_REGION.rdfIri(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(UserNames.Properties.INFLUENCE_FOR_REGION.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.INFLUENCE_FOR_REGION.rdfIri(), RDFS.RANGE, RegionNames.Classes.REGION_LIKE) // POI or Region
                .add(UserNames.Properties.INFLUENCE_FOR_REGION.rdfIri(), RDFS.LABEL, "has influence for region")
                .add(get(), OWL.HASKEY, UserNames.Properties.INFLUENCE_FOR_REGION.rdfIri());

        builder
                .add(UserNames.Properties.INFLUENCE_BY_USER.rdfIri(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(UserNames.Properties.INFLUENCE_BY_USER.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.INFLUENCE_BY_USER.rdfIri(), RDFS.RANGE, UserNames.Classes.USER) // POI or Region
                .add(UserNames.Properties.INFLUENCE_BY_USER.rdfIri(), RDFS.LABEL, "the influence is given by the user (e.g. feedback or history) for the region")
                .add(get(), OWL.HASKEY, UserNames.Properties.INFLUENCE_FOR_REGION.rdfIri());

        builder
                .add(UserNames.Classes.USER_FEEDBACK_INFLUENCE.rdfIri(), RDF.TYPE, OWL.CLASS)
                .add(UserNames.Classes.USER_FEEDBACK_INFLUENCE.rdfIri(), RDFS.SUBCLASSOF, get())
                .add(get(), RDFS.LABEL, "User feedback influence" );


        builder
                .add(UserNames.Classes.USER_HISTORY_INFLUENCE.rdfIri(), RDF.TYPE, OWL.CLASS)
                .add(UserNames.Classes.USER_HISTORY_INFLUENCE.rdfIri(), RDFS.SUBCLASSOF, get())
                .add(get(), RDFS.LABEL, "User feedback influence" );

    }
}
