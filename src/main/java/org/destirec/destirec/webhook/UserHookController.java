package org.destirec.destirec.webhook;

import org.destirec.destirec.rdf4j.services.UserPreferenceService;
import org.destirec.destirec.rdf4j.user.ExternalUserDto;
import org.destirec.destirec.utils.ResponseData;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hook/user")
public class UserHookController {
    private final UserPreferenceService userService;

    public UserHookController(UserPreferenceService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ResponseData<String>> createUser(@RequestBody ExternalUserDto dto) {
        try {
            IRI userIRI = userService.createUser(dto);
            var response = new ResponseData<String>();
            response.setData(userIRI.stringValue());
            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException exception) {
            var response = new ResponseData<String>();
            response.setError(exception.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }

    @PutMapping(value = "/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable String userId, @RequestBody ExternalUserDto user) {
        System.out.println("Id is " + userId);
        System.out.println("User is" + user);

        return new ResponseEntity<>("Success", HttpStatus.ACCEPTED);
    }
}
