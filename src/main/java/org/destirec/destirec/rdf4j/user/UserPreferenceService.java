package org.destirec.destirec.rdf4j.user;

import org.destirec.destirec.rdf4j.attribute.QualityOntology;
import org.destirec.destirec.rdf4j.months.MonthDto;
import org.destirec.destirec.rdf4j.ontology.DestiRecOntology;
import org.destirec.destirec.rdf4j.preferences.PreferenceDao;
import org.destirec.destirec.rdf4j.preferences.PreferenceDto;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.rdf4j.user.apiDto.ExternalPreference;
import org.destirec.destirec.rdf4j.user.apiDto.ExternalUserDto;
import org.eclipse.rdf4j.model.IRI;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// TODO Separation of concerns between user and preference services
@Service
public class UserPreferenceService {
    private final UserDao userDao;
    private final PreferenceDao preferenceDao;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private final UserDtoCreator creator;

    private final QualityOntology qualityOntology;

    private final DestiRecOntology ontology;

    private final ScheduledExecutorService scheduledService;


    public UserPreferenceService(
            UserDao userDao,
            DestiRecOntology ontology,
            UserConfig userConfig, PreferenceDao preferenceDao) {
        this.userDao = userDao;
        creator = userDao
                .getDtoCreator();
        this.ontology = ontology;

        qualityOntology = new QualityOntology(
                ontology,
                ontology.getFactory(),
                userDao.getRdf4JTemplate()
        );
        this.preferenceDao = preferenceDao;

        scheduledService = Executors.newSingleThreadScheduledExecutor();
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


        logger.info("Create user with DTO{}", userDto);
        IRI userId = userDao.saveAndReturnId(userDto);
        logger.info("User with ID {} was created", userId);


        IRI preferenceId = null;
        if (externalUserDto.hasPreferences()) {
            preferenceId = createPreference(externalUserDto, userId);
        }

        return new Pair<>(userId, preferenceId);
    }

    @Transactional
    public PreferenceDto updatePreference(ExternalPreference preference) {
        var featureMaps = preference
                .getFeatures();
        List<FeatureDto> features = featureMaps.entrySet().stream()
                .map((feature) ->
                        userDao.getPreferenceDao().getFeatureDao()
                                .getDtoCreator()
                                .createFromTuple(feature.getKey(), feature.getValue()))
                .map(featureDto -> userDao.getPreferenceDao().getFeatureDao().save(featureDto))
                .toList();

        IRI userId = userDao.getDtoCreator().createId(preference.getUserId());
        if (userDao.getByIdOptional(userId).isEmpty()) {
            throw new IllegalArgumentException("Cannot find user for updating it");
        }

        IRI preferenceId = preferenceDao.getByAuthorId(userId);

        if (preferenceId == null) {
            throw new IllegalArgumentException("Cannot find preference for updating it");
        }
        // remove all features
        preferenceDao.getFeatureDao().removeByHasFeatureConnection(preferenceId);
        preferenceDao.removeFeatureQualities(preferenceId);

        PreferenceDto preferenceDto = userDao.getPreferenceDao().getDtoCreator()
        .create(preferenceId, userId, features, new ArrayList<>(), null);
        preferenceDto = userDao.getPreferenceDao().save(preferenceDto);

        scheduledUpdateOfOntologies(preferenceDto);

        logger.info("Preference with id {} was updated", preferenceId);
        return preferenceDto;
    }

    private void updateOntologies(PreferenceDto preferenceDto) {
        ontology.removeAxiomSet(preferenceDto.getId().stringValue());
        qualityOntology.definePreferenceQualities(preferenceDto, preferenceDto.getId().stringValue());
        ontology.migrate(preferenceDto.getId().stringValue());
        ontology.triggerInference();
    }

    @Async
    public void scheduledUpdateOfOntologies(PreferenceDto preferenceDto) {
        updateOntologies(preferenceDto);
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

        // update ontologies
        scheduledUpdateOfOntologies(preferenceDto);

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
        logger.info("Update user with DTO {}", userDto);
        IRI userId = userDao.saveAndReturnId(userDto, userDtoOptional.get().id());
        logger.info("User with id {} was updated", userId);
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

    @Transactional
    public PreferenceDto getPreferenceForUser(String userId) {
        return preferenceDao
                .getByAuthor(userDao.getDtoCreator().createId(userId))
                .orElse(null);
    }


    @Transactional
    public List<UserDto> getUsers(UserController.UserPaginationRequest paginationRequest) {
        return userDao.listPaginated(paginationRequest.page(), paginationRequest.size());
    }
 }
