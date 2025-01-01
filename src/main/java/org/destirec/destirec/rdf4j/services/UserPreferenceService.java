package org.destirec.destirec.rdf4j.services;

import org.destirec.destirec.rdf4j.preferences.PreferenceDao;
import org.destirec.destirec.rdf4j.preferences.PreferenceDto;
import org.destirec.destirec.rdf4j.user.UserDao;
import org.destirec.destirec.rdf4j.user.UserDto;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPreferenceService {
    private final UserDao userDao;
    private final PreferenceDao preferenceDao;


    public UserPreferenceService(UserDao userDao, PreferenceDao preferenceDao) {
        this.userDao = userDao;
        this.preferenceDao = preferenceDao;
    }

    @Transactional
    public IRI addUser(UserDto user) {
        return userDao.saveAndReturnId(user);
    }

    @Transactional
    public IRI addPreference(PreferenceDto preferenceDto) {
        System.out.println(preferenceDto);
        return preferenceDao.saveAndReturnId(preferenceDto);
    }
 }
