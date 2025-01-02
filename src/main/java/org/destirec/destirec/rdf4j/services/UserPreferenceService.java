package org.destirec.destirec.rdf4j.services;

import org.destirec.destirec.rdf4j.preferences.PreferenceDao;
import org.destirec.destirec.rdf4j.preferences.PreferenceDto;
import org.destirec.destirec.rdf4j.preferences.months.MonthDao;
import org.destirec.destirec.rdf4j.preferences.months.MonthDto;
import org.destirec.destirec.rdf4j.user.UserDao;
import org.destirec.destirec.rdf4j.user.UserDto;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserPreferenceService {
    private final UserDao userDao;
    private final PreferenceDao preferenceDao;

    private final MonthDao monthDao;


    public UserPreferenceService(UserDao userDao, PreferenceDao preferenceDao, MonthDao monthDao) {
        this.userDao = userDao;
        this.preferenceDao = preferenceDao;
        this.monthDao = monthDao;
    }

    @Transactional
    public IRI addUser(UserDto user) {
        return userDao.saveAndReturnId(user);
    }

    @Transactional
    public IRI addPreference(PreferenceDto preferenceDto) {
        List<MonthDto> monthDtos = new ArrayList<>(preferenceDto.getMonthsDto());

        for (int i = 0; i < monthDtos.size(); i++) {
            MonthDto month = monthDtos.get(i);
            IRI id = monthDao.saveAndReturnId(month);
            MonthDto newMonthDto = new MonthDto(id, month.month(), month.monthRange());
            monthDtos.set(i, newMonthDto);
        }

        preferenceDto.setMonthsDto(monthDtos);
        return preferenceDao.saveAndReturnId(preferenceDto);
    }
 }
