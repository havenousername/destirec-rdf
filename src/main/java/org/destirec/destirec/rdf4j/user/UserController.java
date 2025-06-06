package org.destirec.destirec.rdf4j.user;

import jakarta.validation.constraints.Min;
import org.destirec.destirec.rdf4j.preferences.PreferenceDto;
import org.destirec.destirec.rdf4j.user.apiDto.ExternalPreference;
import org.destirec.destirec.rdf4j.user.apiDto.ExternalUserDto;
import org.destirec.destirec.utils.ResponseData;
import org.eclipse.rdf4j.model.IRI;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserPreferenceService userService;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public record UserPaginationRequest(
            @DefaultValue("0") @Min(0) Integer page,
            @DefaultValue("10") @Min(1) Integer size,
            @DefaultValue("id") String sortBy,
            @DefaultValue("asc") String sortDir,
            Boolean allowNull
    ) {

        public UserPaginationRequest {
            allowNull = Optional.ofNullable(allowNull).orElse(false);
            if (!allowNull) {
                page = (page == null) ? 0 : page;
                size = (size == null) ? 10 : size;
                sortBy = (sortBy == null) ? "id" : sortBy;
                sortDir = (sortDir == null) ? "asc" : sortDir;
            }
        }
    }

    public UserController(UserPreferenceService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ResponseData<String>> createUser(@RequestBody ExternalUserDto dto) {
        try {
            Pair<IRI, IRI> user = userService.createUser(dto);
            var response = new ResponseData<String>();
            response.setData(user.toString());
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

    @GetMapping("/preference/{preferenceId}")
    public ResponseEntity<PreferenceDto> getPreference(@PathVariable String preferenceId) {
        return ResponseEntity.ok(userService.getPreference(preferenceId));
    }

    @GetMapping("/{userId}/preference")
    public ResponseEntity<PreferenceDto> getUserPreference(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getPreferenceForUser(userId));
    }

    @PutMapping("/preference")
    public ResponseEntity<PreferenceDto> updatePreference(
            @RequestBody ExternalPreference preferenceDto
    ) {
        try {
            return ResponseEntity.ok(userService.updatePreference(preferenceDto));
        } catch (Exception exception) {
            logger.error(exception.toString());
            exception.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body(null);
        }
    }

    @PutMapping(value = "/{userId}")
    public ResponseEntity<ResponseData<String>> updateUser(@PathVariable String userId, @RequestBody ExternalUserDto user) {
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

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(@ModelAttribute UserPaginationRequest paginationRequest) {
        try {
            return ResponseEntity.ok(userService.getUsers(paginationRequest));
        }  catch (Exception exception) {
            logger.error(exception.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(null);
        }
    }
}
