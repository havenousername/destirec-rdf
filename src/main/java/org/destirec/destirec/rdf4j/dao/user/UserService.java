package org.destirec.destirec.rdf4j.dao.user;

import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserDao userDao;


    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Transactional
    public IRI addUser(UserDto user) {
        return userDao.saveAndReturnId(user);
    }
}
