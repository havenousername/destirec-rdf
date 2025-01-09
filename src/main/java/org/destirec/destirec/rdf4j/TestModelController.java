package org.destirec.destirec.rdf4j;

import org.destirec.destirec.rdf4j.model.ModelRDF;
import org.destirec.destirec.rdf4j.model.resource.User;
import org.destirec.destirec.rdf4j.model.resource.UserPreferences;
import org.destirec.destirec.rdf4j.preferences.PreferenceDtoCreator;
import org.destirec.destirec.rdf4j.preferences.PreferenceModel;
import org.destirec.destirec.rdf4j.preferences.months.MonthDao;
import org.destirec.destirec.rdf4j.services.UserPreferenceService;
import org.destirec.destirec.rdf4j.user.UserDto;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

@RestController
public class TestModelController {
    ModelRDF modelRDF = new ModelRDF();

    private final UserPreferenceService userService;
    private final PreferenceDtoCreator preferenceDtoCreator;

    public TestModelController(
            UserPreferenceService userService,
            MonthDao monthDao
    ) {
        this.userService = userService;
        preferenceDtoCreator = new PreferenceDtoCreator(monthDao);
    }




    private void createUserClassResources() {
        modelRDF.getUserClass().createResource(
                modelRDF.getBuilder(),
                modelRDF.getGraphName()
        );
        modelRDF.getUserClass().addPropertiesToResource(
                modelRDF.getBuilder(),
                modelRDF.getGraphName(),
                modelRDF.getUserClass().getLastResource(),
                Map.ofEntries(
                        Map.entry(User.Fields.NAME, "Andrei"),
                        Map.entry(User.Fields.EMAIL, "andrei@gmail.com")
                )
        );
    }

    private void createUserPreferencesResources() {
        ArrayList<Map<UserPreferences.Fields, String>> resources = new ArrayList<>();

        resources.add(Map.ofEntries(
                Map.entry(UserPreferences.Fields.MONTH, "--01"),
                Map.entry(UserPreferences.Fields.IS_PEAK_SEASON_IMPORTANT, "true"),
                Map.entry(UserPreferences.Fields.MONTHS_RANGE, "80")
        ));

        resources.add(Map.ofEntries(
                Map.entry(UserPreferences.Fields.MONTH, "--02")
        ));

        modelRDF.getUserPreferences()
                .createResource(
                        modelRDF.getBuilder(),
                        modelRDF.getGraphName()
                );

        modelRDF.getUserPreferences().addPropertiesToResource(
                modelRDF.getBuilder(),
                modelRDF.getGraphName(),
                modelRDF.getUserPreferences().getLastResource(),
                resources
        );

        modelRDF.getUserPreferences().addPropertiesToResource(
                modelRDF.getBuilder(),
                modelRDF.getGraphName(),
                modelRDF.getUserPreferences().getLastResource(),
                Map.ofEntries(
                        Map.entry(UserPreferences.Fields.POPULARITY_RANGE, "89"),
                        Map.entry(UserPreferences.Fields.IS_POPULARITY_IMPORTANT, "true")
                )
        );
    }

    @GetMapping(value = "/add-user", produces = "text/turtle")
    public String testDao() {
        IRI userIRI = userService.addUser(
                new UserDto("Andrei", "andrei997", "andrei.cristea@gmail.com", "Worker")
        );
        var preferences = Map.of(
                PreferenceModel.Fields.PRICE_RANGE, "50",
                PreferenceModel.Fields.IS_PRICE_IMPORTANT, "true",
                PreferenceModel.Fields.POPULARITY_RANGE, "79",
                PreferenceModel.Fields.IS_POPULARITY_IMPORTANT, "false"
        );

        Float[] randomNumbers = new Random()
                .doubles(12, 0, 100)
                .mapToObj(d -> (float) d)
                .toArray(Float[]::new);

//        return userService.getUser(userIRI).toString();
        return userService.addPreference(preferenceDtoCreator
                .create(null, userIRI, preferences, randomNumbers)).toString();
    }

    @GetMapping(path = "/test-rdf-model", produces = "text/turtle")
    public String testRdfModel() {
        createUserClassResources();
        createUserPreferencesResources();

        return modelRDF.toString();
    }
}
