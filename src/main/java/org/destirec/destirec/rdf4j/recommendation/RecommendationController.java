package org.destirec.destirec.rdf4j.recommendation;

import org.destirec.destirec.rdf4j.user.UserDto;
import org.destirec.destirec.rdf4j.user.UserPreferenceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendation")
public class RecommendationController {
    private final RecommendationService service;
    private final UserPreferenceService userPreferenceService;

    public RecommendationController(RecommendationService service, UserPreferenceService userPreferenceService) {
        this.service = service;
        this.userPreferenceService = userPreferenceService;
    }


    @GetMapping("/simple")
    public ResponseEntity<Recommendation> getSimpleRecommendation(
            @RequestHeader("X-Anonymous-Token") String authHeader
    ) {
        UserDto user = userPreferenceService.getUser(authHeader);
        if (user == null) {
            throw new RuntimeException("X-Anonymous-Token did not provide correct anonymous user for the recommendations" +
                    ". Please try again ");
        }
        return ResponseEntity.ok(service.getSimpleRecommendation(
                RecommendationParameters.getDefault(), user));
    }

    @GetMapping(value="/greater-than", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Recommendation> getGreaterThanRecommendation(
            @ModelAttribute RecommendationParameters parameters,
            @RequestHeader("X-Anonymous-Token") String authHeader
    ) {
        UserDto user = userPreferenceService.getUser(authHeader);
        if (user == null) {
            throw new RuntimeException("X-Anonymous-Token did not provide correct anonymous user for the recommendations" +
                    ". Please try again ");
        }
        var recommendations = service.getBiggerThanRecommendation(parameters, user);
        return ResponseEntity.ok(recommendations);
    }


    @GetMapping("/la")
    public ResponseEntity<Void> getLARecommendation(
            @ModelAttribute RecommendationParameters parameters,
            @RequestHeader("X-Anonymous-Token") String authHeader
    ) {
        UserDto user = userPreferenceService.getUser(authHeader);
        if (user == null) {
            throw new RuntimeException("X-Anonymous-Token did not provide correct anonymous user for the recommendations" +
                    ". Please try again ");
        }
        return ResponseEntity.ok(service.getLARecommendation(parameters, user));
    }
}
