package org.destirec.destirec.rdf4j.region;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.destirec.destirec.rdf4j.interfaces.ResponsePaginated;
import org.destirec.destirec.rdf4j.region.apiDto.ExternalRegionDto;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.utils.ResponseData;
import org.destirec.destirec.utils.rdfDictionary.RegionNames.Individuals.RegionTypes;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/region")
public class RegionController {
    public record PaginationRequest(
            @DefaultValue("0") @Min(0) Integer page,
            @DefaultValue("10") @Min(1) @Max(100) Integer size,
            @DefaultValue("id") String sortBy,
            @DefaultValue("asc") String sortDir,
            String regionType
    ) {

        public PaginationRequest {
            page = (page == null) ? 0 : page;
            size = (size == null) ? 10 : size;
            sortBy = (sortBy == null) ? "id" : sortBy;
            sortDir = (sortDir == null) ? "asc" : sortDir;
        }

        public RegionTypes getRegionType() {
            try {
                return RegionTypes.fromString(regionType);
            } catch (Exception exception) {
                return null;
            }
        }

        public boolean isAscending() {
            return sortDir.equals("asc");
        }
    }


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
            response.setError(exception.getMessage());;

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }

    @GetMapping("/leaf")
    public ResponseEntity<List<RegionDto>> getLeafRegions() {
        return ResponseEntity.ok(regionService.getLeafRegions());
    }

    @GetMapping("/costs")
    public ResponseEntity<List<CostDto>> getCosts() {
        return ResponseEntity.ok(regionService.getCosts());
    }

    @PutMapping(value = "/{regionId}")
    public ResponseEntity<ResponseData<String>> updateRegion(@RequestBody ExternalRegionDto dto, @PathVariable String regionId) {
        try {
            IRI updatedRegionIRI = regionService.updateRegion(dto);
            var response = new ResponseData<String>();
            response.setData(updatedRegionIRI.stringValue());
            return ResponseEntity.ok(response);
        }  catch (Exception exception) {
            var response = new ResponseData<String>();
            response.setData(exception.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }

    @GetMapping(value = "/sparql/select")
    public ResponseEntity<String> getSparqlSelect() {
        return ResponseEntity.ok(regionService.getRegionSelect());
    }

    @GetMapping(value = "/{regionId}")
    public Optional<RegionDto> getRegionDto(@PathVariable String regionId) {
        return regionService.getRegion(regionId);
    }

    @GetMapping
    public ResponseEntity<ResponsePaginated<List<RegionDto>>> getRegions(@ModelAttribute PaginationRequest pagination) {
        List<RegionDto> regions;
        long total;
        if (pagination.getRegionType() != null) {
            regions = regionService.getRegionsByType(
                    pagination.getRegionType(),
                    pagination.page,
                    pagination.size
            );
            total = regionService.getTotalRegionsByType(pagination.getRegionType());
        } else {
            regions = regionService.getRegions(
                    pagination.page,
                    pagination.size,
                    pagination.sortBy,
                    pagination.sortDir
            );
            total = regionService.getTotalRegions();
        }

        ResponsePaginated<List<RegionDto>> responsePaginated = new ResponsePaginated<>(
                regions,new ResponsePaginated.Pagination(pagination.page, total, pagination.size)

        );

        return ResponseEntity.ok(responsePaginated);
    }
}
