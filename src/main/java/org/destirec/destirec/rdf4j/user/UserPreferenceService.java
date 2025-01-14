package org.destirec.destirec.rdf4j.user;

import org.destirec.destirec.rdf4j.months.MonthDao;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.preferences.PreferenceDao;
import org.destirec.destirec.rdf4j.preferences.PreferenceDto;
import org.destirec.destirec.rdf4j.user.apiDto.CreateUserDto;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO Separation of concerns between user and preference services
@Service
public class UserPreferenceService {
    private final UserDao userDao;
    private final PreferenceDao preferenceDao;

    private final MonthDao monthDao;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private final UserDtoCreator creator;


    public UserPreferenceService(
            UserDao userDao,
            PreferenceDao preferenceDao,
            MonthDao monthDao
    ) {
        this.userDao = userDao;
        this.preferenceDao = preferenceDao;
        this.monthDao = monthDao;
        creator =  (UserDtoCreator)userDao
                .getDtoCreator();
    }

    @Transactional
    public IRI createUser(CreateUserDto createUserDto) {
        UserDto userDto = creator.create(createUserDto);

        Optional<UserDto> existUser = userDao.getByIdOptional(userDto.id());
        if (existUser.isPresent()) {
            String msg = "User with ID " + existUser.get().id() + " is already present in the RDF database";
            logger.info(msg);
            throw new IllegalArgumentException(msg);
        }

        logger.info("Create user with DTO" + userDto);
        IRI userId = userDao.saveAndReturnId(userDto);
        logger.info("User with ID " + userId + " was created");
        return userId;
    }

    @Transactional
    public IRI updateUser(String id, CreateUserDto user) {
        UserDto userDto = creator.create(id, user);
        Optional<UserDto> userDtoOptional = userDao.getByIdOptional(userDto.id());
        if (userDtoOptional.isEmpty()) {
            throw new IllegalArgumentException("Cannot find user for updating it");
        }
        logger.info("Update user with DTO " + userDto);
        IRI userId = userDao.saveAndReturnId(userDto, userDtoOptional.get().id());
        logger.info("User with id " + userId + " was updated");
        return userId;
    }

    @Transactional
    public IRI addUser(UserDto user) {
        return userDao.saveAndReturnId(user);
    }

    @Transactional
    public UserDto getUser(String id) {
        return userDao.getById(creator.createId(id));
    }

    @Transactional
    public PreferenceDto addPreference(PreferenceDto preferenceDto) {
        List<MonthDto> monthDtos = new ArrayList<>(preferenceDto.getMonthsDto());

        for (int i = 0; i < monthDtos.size(); i++) {
            MonthDto month = monthDtos.get(i);
            IRI id = monthDao.saveAndReturnId(month);
            MonthDto newMonthDto = new MonthDto(id, month.month(), month.monthRange());
            monthDtos.set(i, newMonthDto);
        }

        preferenceDto.setMonthsDto(monthDtos);
//        PreferenceDto preference = preferenceDao.save(preferenceDto);
//        System.out.println(preference);
//        System.out.println(preferenceDao.getReadQuery());
        return preferenceDao.save(preferenceDto);
    }
 }
