package org.destirec.destirec.rdf4j.userScores;

import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.destirec.destirec.utils.rdfDictionary.TopOntologyNames;
import org.destirec.destirec.utils.rdfDictionary.UserNames;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;

public class UserActivityMigration extends IriMigration {
    protected UserActivityMigration(RDF4JTemplate rdf4jMethods) {
        super(rdf4jMethods, UserNames.Classes.USER_ACTIVITY.str());
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.CLASS)
                .add(get(), RDFS.SUBCLASSOF, TopOntologyNames.Classes.EVENT.rdfIri())
                .add(get(), RDFS.LABEL, "User activity superclass" );


        // has activity over user
        builder
                .add(UserNames.Properties.HAS_ACTIVITY_OVER_ENTITY.rdfIri(), RDF.TYPE, OWL.OBJECTPROPERTY)
                .add(UserNames.Properties.HAS_ACTIVITY_OVER_ENTITY.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.HAS_ACTIVITY_OVER_ENTITY.rdfIri(), RDFS.RANGE, RegionNames.Classes.REGION_LIKE) // POI or Region
                .add(UserNames.Properties.HAS_ACTIVITY_OVER_ENTITY.rdfIri(), RDFS.LABEL, "has visited entity")
                .add(get(), OWL.HASKEY, UserNames.Properties.HAS_ACTIVITY_OVER_ENTITY.rdfIri());


        builder
                .add(UserNames.Properties.HAS_ACTIVITY_P_SCORE.rdfIri(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(UserNames.Properties.HAS_ACTIVITY_P_SCORE.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.HAS_ACTIVITY_P_SCORE.rdfIri(), RDFS.RANGE, XSD.DOUBLE) // POI or Region
                .add(UserNames.Properties.HAS_ACTIVITY_P_SCORE.rdfIri(), RDFS.LABEL, "has p score entity")
                .add(get(), OWL.HASKEY, UserNames.Properties.HAS_ACTIVITY_P_SCORE.rdfIri());


        builder
                .add(UserNames.Properties.HAS_USER.rdfIri(), RDF.TYPE, OWL.OBJECTPROPERTY)
                .add(UserNames.Properties.HAS_USER.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.HAS_USER.rdfIri(), RDFS.RANGE, UserNames.Classes.USER.rdfIri())
                .add(UserNames.Properties.HAS_USER.rdfIri(), RDFS.LABEL, "has user")
                .add(get(), OWL.HASKEY, UserNames.Properties.HAS_USER.rdfIri()); // Mandatory


        builder
                .add(UserNames.Properties.HAS_INFLUENCE.rdfIri(), RDF.TYPE, OWL.OBJECTPROPERTY)
                .add(UserNames.Properties.HAS_INFLUENCE.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.HAS_INFLUENCE.rdfIri(), RDFS.RANGE, UserNames.Classes.USER_INFLUENCE.rdfIri())
                .add(UserNames.Properties.HAS_INFLUENCE.rdfIri(), RDFS.LABEL, "connects to user influence");

        builder
                .add(UserNames.Properties.HAS_TIME_FROM.rdfIri(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(UserNames.Properties.HAS_TIME_FROM.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.HAS_TIME_FROM.rdfIri(), RDFS.RANGE, XSD.DATETIME)
                .add(UserNames.Properties.HAS_TIME_FROM.rdfIri(), RDFS.LABEL, "time from when the trip started");



        builder
                .add(UserNames.Properties.HAS_TIME_TO.rdfIri(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(UserNames.Properties.HAS_TIME_TO.rdfIri(), RDFS.DOMAIN, get())
                .add(UserNames.Properties.HAS_TIME_TO.rdfIri(), RDFS.RANGE, XSD.DATETIME)
                .add(UserNames.Properties.HAS_TIME_TO.rdfIri(), RDFS.LABEL, "time when the trip ended");
    }
}
