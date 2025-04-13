package org.destirec.destirec.rdf4j.region.feature;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.IriMigration;
import org.destirec.destirec.rdf4j.vocabulary.DBPEDIA;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Component;

@Component
@Getter
public class FeatureMigration extends IriMigration {
    public FeatureMigration(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate, "Feature");
    }

    @Override
    protected void setupProperties() {
        builder
                .add(get(), RDF.TYPE, OWL.DATATYPEPROPERTY)
                .add(get(), RDFS.SUBPROPERTYOF, DBPEDIA.INTEREST)
                .add(get(), RDFS.RANGE, RDFS.RESOURCE);
    }
}
