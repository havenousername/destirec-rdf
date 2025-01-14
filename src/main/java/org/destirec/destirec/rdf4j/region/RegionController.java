package org.destirec.destirec.rdf4j.region;

import org.destirec.destirec.rdf4j.region.apiDto.ExternalRegionDto;
import org.destirec.destirec.utils.ResponseData;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/region")
public class RegionController {
    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @PostMapping
    public ResponseEntity<ResponseData<String>> createRegion(@RequestBody ExternalRegionDto dto) {
        try {
            IRI regionIRI = regionService.createRegion(dto);
            var response = new ResponseData<String>();
            response.setData(regionIRI.stringValue());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException exception) {
            var response = new ResponseData<String>();
            response.setError(exception.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }
}
