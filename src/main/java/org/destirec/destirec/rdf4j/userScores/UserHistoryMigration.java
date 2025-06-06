package org.destirec.destirec.rdf4j.userScores;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.utils.rdfDictionary.UserNames;
import org.eclipse.rdf4j.model.vocabulary.*;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

@Component
@Getter
public class UserHistoryMigration extends IriMigration {
    public UserHistoryMigration(RDF4JTemplate rdf4jMethods) {
        super(rdf4jMethods, UserNames.Classes.USER_HISTORY.str());
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, UserNames.Classes.USER_ACTIVITY.rdfIri())
                .add(get(), RDFS.LABEL, "User History" );
        builder
                .add(UserNames.Properties.HAS_VISITED_P_SCORE.rdfIri(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(UserNames.Properties.HAS_VISITED_P_SCORE.rdfIri(), RDFS.SUBPROPERTYOF, UserNames.Properties.HAS_ACTIVITY_P_SCORE.rdfIri())
                .add(UserNames.Properties.HAS_VISITED_P_SCORE.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.HAS_VISITED_P_SCORE.rdfIri(), RDFS.LABEL, "has visited p score")
                .add(get(), OWL.HASKEY, UserNames.Properties.HAS_VISITED_P_SCORE.rdfIri()); // Mandatory

        // Define properties for USER_HISTORY
        // Property: hasVisitedEntity (links to a POI or Region)
        builder
                .add(UserNames.Properties.HAS_VISITED_ENTITY.rdfIri(), RDF.TYPE, OWL.OBJECTPROPERTY)
                .add(UserNames.Properties.HAS_VISITED_ENTITY.rdfIri(), RDFS.SUBPROPERTYOF, UserNames.Properties.HAS_ACTIVITY_OVER_ENTITY.rdfIri())
                .add(UserNames.Properties.HAS_VISITED_ENTITY.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.HAS_VISITED_ENTITY.rdfIri(), RDFS.LABEL, "has visited entity")
                .add(get(), OWL.HASKEY, UserNames.Properties.HAS_VISITED_ENTITY.rdfIri());


        builder
                .add(UserNames.Properties.HAS_INFLUENCE.rdfIri(), RDF.TYPE, OWL.OBJECTPROPERTY)
                .add(UserNames.Properties.HAS_INFLUENCE.rdfIri(), RDFS.RANGE, UserNames.Classes.USER_FEEDBACK_INFLUENCE.rdfIri())
                .add(UserNames.Properties.HAS_INFLUENCE.rdfIri(), RDFS.LABEL, "connects from user history to user influence");
    }
}
