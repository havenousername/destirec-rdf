package org.destirec.destirec.rdf4j.dao.user;

import org.destirec.destirec.rdf4j.dao.interfaces.GenericDao;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.springframework.stereotype.Repository;




@Repository
public class UserDao extends GenericDao<UserModel.Fields, UserDto> {
    public UserDao(
            RDF4JTemplate rdf4JTemplate,
            UserModel model,
            UserMigration userMigration,
            UserDtoCreator userDtoCreator
    ) {
        super(rdf4JTemplate, model, userMigration, userDtoCreator);
    }
}
