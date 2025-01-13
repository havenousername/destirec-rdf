package org.destirec.destirec.rdf4j.user;

import org.destirec.destirec.rdf4j.interfaces.GenericDao;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao extends GenericDao<UserConfig.Fields, UserDto> {
    public UserDao(
            RDF4JTemplate rdf4JTemplate,
            UserConfig model,
            UserMigration userMigration,
            UserDtoCreator userDtoCreator
    ) {
        super(rdf4JTemplate, model, userMigration, userDtoCreator);
    }
}
