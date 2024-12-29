package org.destirec.destirec.rdf4j;

import org.destirec.destirec.rdf4j.dao.user.UserDao;
import org.destirec.destirec.rdf4j.dao.user.UserDto;
import org.destirec.destirec.rdf4j.dao.user.UserMigration;
import org.destirec.destirec.rdf4j.dao.user.UserService;
import org.destirec.destirec.rdf4j.model.ModelRDF;
import org.destirec.destirec.rdf4j.model.resource.User;
import org.destirec.destirec.rdf4j.model.resource.UserPreferences;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;

@RestController
public class TestModelController {
    ModelRDF modelRDF = new ModelRDF();

    private final UserDao userDao;

    private final UserMigration userMigration;

    private final UserService userService;

    public TestModelController(UserDao userDao, UserMigration userMigration, UserService userService) {
        this.userDao = userDao;
        this.userMigration = userMigration;
        this.userService = userService;
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
        return userService.addUser(new UserDto("Andrei", "andrei997", "andrei.cristea@gmail.com", "Worker")).stringValue();
    }

    @GetMapping(path = "/test-rdf-model", produces = "text/turtle")
    public String testRdfModel() {
        createUserClassResources();
        createUserPreferencesResources();

        return modelRDF.toString();
    }
}
