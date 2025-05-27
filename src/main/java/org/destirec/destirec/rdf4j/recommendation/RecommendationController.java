package org.destirec.destirec.rdf4j.recommendation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendation")
public class RecommendationController {
    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }


    @GetMapping("/simple")
    public ResponseEntity<Recommendation> getSimpleRecommendation() {
        return ResponseEntity.ok(service.getSimpleRecommendation());
    }

    @GetMapping("/greater-than")
    public ResponseEntity<Recommendation> getGreaterThanRecommendation(@ModelAttribute RecommendationParameters parameters) {
        return ResponseEntity.ok(service.getBiggerThanRecommendation(parameters));
    }
}
