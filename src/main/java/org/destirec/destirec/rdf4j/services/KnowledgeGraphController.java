package org.destirec.destirec.rdf4j.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/kg")
public class KnowledgeGraphController {
    private final KnowledgeGraphService knowledgeGraphService;

    public KnowledgeGraphController(KnowledgeGraphService knowledgeGraphService) {
        this.knowledgeGraphService = knowledgeGraphService;
    }

    @GetMapping("/regions")
    public ResponseEntity<List<Map<String, Object>>> getAllRegionEntities() {
        return ResponseEntity.ok(knowledgeGraphService.getAllRegionEntities());
    }
}
