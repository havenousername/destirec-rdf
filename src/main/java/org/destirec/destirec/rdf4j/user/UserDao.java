package org.destirec.destirec.rdf4j.user;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.preferences.PreferenceDao;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

@Getter
@Repository
public class UserDao extends GenericDao<UserConfig.Fields, UserDto> {
    private final PreferenceDao preferenceDao;
    public UserDao(
            RDF4JTemplate rdf4JTemplate,
            UserConfig model,
            UserMigration userMigration,
            UserDtoCreator userDtoCreator,
            PreferenceDao preferenceDao,
            DestiRecOntology ontology
    ) {
        super(rdf4JTemplate, model, userMigration, userDtoCreator, ontology);
        this.preferenceDao = preferenceDao;
    }
}
