package org.destirec.destirec.rdf4j.user;

import org.destirec.destirec.rdf4j.user.apiDto.CreateUserDto;
import org.destirec.destirec.utils.ResponseData;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserPreferenceService userService;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public UserController(UserPreferenceService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ResponseData<String>> createUser(@RequestBody CreateUserDto dto) {
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
    public ResponseEntity<ResponseData<String>> updateUser(@PathVariable String userId, @RequestBody CreateUserDto user) {
        try {
            IRI updatedUserIRI = userService.updateUser(userId, user);
            var response = new ResponseData<String>();
            response.setData(updatedUserIRI.stringValue());
            return ResponseEntity.ok(response);
        }
        catch (Exception exception) {
            var response  = new ResponseData<String>();
            response.setError(exception.getMessage());
            return ResponseEntity.
                    badRequest()
                    .body(response);
        }
    }
}
