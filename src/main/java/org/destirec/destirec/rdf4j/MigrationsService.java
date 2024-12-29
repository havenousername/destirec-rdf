package org.destirec.destirec.rdf4j;

import jakarta.annotation.PostConstruct;
import org.destirec.destirec.rdf4j.dao.user.UserMigration;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.destirec.destirec.utils.Constants.DEFAULT_GRAPH;

@Service
public class MigrationsService {
    private final UserMigration userMigration;


    public MigrationsService(UserMigration userMigration) {
        this.userMigration = userMigration;
        userMigration.setGraphName(DEFAULT_GRAPH);
        userMigration.setNamespaces(List.of(RDFS.NS, OWL.NS, RDF.NS, XSD.NS));
    }

    @PostConstruct
    public void runMigrations() {
        userMigration.setup();
        userMigration.migrate();
    }
}
