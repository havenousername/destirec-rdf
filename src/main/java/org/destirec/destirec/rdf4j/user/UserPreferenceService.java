package org.destirec.destirec.rdf4j.user;

import org.destirec.destirec.rdf4j.attribute.QualityOntology;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.preferences.PreferenceDto;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.rdf4j.user.apiDto.ExternalUserDto;
import org.eclipse.rdf4j.model.IRI;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// TODO Separation of concerns between user and preference services
@Service
public class UserPreferenceService {
    private final UserDao userDao;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private final UserDtoCreator creator;

    private final QualityOntology qualityOntology;

    private final DestiRecOntology ontology;


    public UserPreferenceService(
            UserDao userDao,
            DestiRecOntology ontology
    ) {
        this.userDao = userDao;
        creator =  (UserDtoCreator)userDao
                .getDtoCreator();
        this.ontology = ontology;

        qualityOntology = new QualityOntology(
                ontology,
                ontology.getFactory(),
                userDao.getRdf4JTemplate()
        );
    }

    @Transactional
    public Pair<IRI, IRI> createUser(ExternalUserDto externalUserDto) {
        UserDto userDto = creator.create(externalUserDto);
        Optional<UserDto> existUser = userDao.getByIdOptional(userDto.id());
        if (existUser.isPresent()) {
            String msg = "User with ID " + existUser.get().id() + " is already present in the RDF database";
            logger.info(msg);
            throw new IllegalArgumentException(msg);
        }


        logger.info("Create user with DTO" + userDto);
        IRI userId = userDao.saveAndReturnId(userDto);
        logger.info("User with ID " + userId + " was created");


        IRI preferenceId = null;
        if (externalUserDto.hasPreferences()) {
            preferenceId = createPreference(externalUserDto, userId);
        }

        return new Pair<>(userId, preferenceId);
    }

    @Transactional
    public IRI createPreference(ExternalUserDto externalUserDto, IRI userId) {
        List<FeatureDto> features = externalUserDto
                .getFeatures().entrySet().stream()
                .map((feature) ->
                        userDao.getPreferenceDao().getFeatureDao()
                                .getDtoCreator()
                                .createFromTuple(feature.getKey(), feature.getValue()))
                .map(featureDto -> userDao.getPreferenceDao().getFeatureDao().save(featureDto))
                .toList();

        List<MonthDto> months = externalUserDto
                .getMonths().entrySet().stream()
                .map(month ->
                        userDao.getPreferenceDao().getMonthDao()
                                .getDtoCreator()
                                .create(month))
                .map(month ->
                        userDao.getPreferenceDao().getMonthDao().save(month))
                .toList();

        Triplet<Integer, Integer, Boolean> costTriplet = externalUserDto
                .getCost();
        CostDto costDto = userDao.getPreferenceDao()
                .getCostDao()
                .getDtoCreator()
                .create(costTriplet.getValue0(), costTriplet.getValue1(), costTriplet.getValue2());
        costDto = userDao.getPreferenceDao().getCostDao().save(costDto);

        PreferenceDto preferenceDto = userDao.getPreferenceDao().getDtoCreator()
                .create(userId, features, months, costDto);
        preferenceDto = userDao.getPreferenceDao().save(preferenceDto);
        qualityOntology.definePreferenceQualities(preferenceDto, preferenceDto.getId().stringValue());
        ontology.migrate(preferenceDto.getId().stringValue());
        ontology.triggerInference();

        return preferenceDto.getId();
    }

    @Transactional
    public IRI updateUser(String id, ExternalUserDto user) {
        UserDto userDto = creator.create(id, user);
        System.out.println(userDto.id() + " IS ID\n");
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
    public PreferenceDto getPreference(String id) {
        return userDao.getPreferenceDao().getById(
                userDao.getPreferenceDao().getDtoCreator().createId(id)
        );
    }
 }
