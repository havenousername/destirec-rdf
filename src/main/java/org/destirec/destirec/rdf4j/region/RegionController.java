package org.destirec.destirec.rdf4j.region;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import org.destirec.destirec.rdf4j.interfaces.ResponsePaginated;
import org.destirec.destirec.rdf4j.region.apiDto.ExternalRegionDto;
import org.destirec.destirec.rdf4j.region.apiDto.ResponseRegionDto;
import org.destirec.destirec.rdf4j.region.cost.CostDto;
import org.destirec.destirec.utils.ResponseData;
import org.destirec.destirec.utils.rdfDictionary.RegionNames.Individuals.RegionTypes;
import org.eclipse.rdf4j.model.IRI;
import org.javatuples.Pair;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/region")
public class RegionController {
    public record PaginationRequest(
            @DefaultValue("0") @Min(0) Integer page,
            @DefaultValue("10") @Min(1) Integer size,
            @DefaultValue("id") String sortBy,
            @DefaultValue("asc") String sortDir,
            String regionType,
            Boolean allowNull
    ) {

        public PaginationRequest {
            allowNull = Optional.ofNullable(allowNull).orElse(false);
            if (!allowNull) {
                page = (page == null) ? 0 : page;
                size = (size == null) ? 10 : size;
                sortBy = (sortBy == null) ? "id" : sortBy;
                sortDir = (sortDir == null) ? "asc" : sortDir;
            }
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

    public String getBaseUrl() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new IllegalStateException("No request context available");
        }

        HttpServletRequest request = attrs.getRequest();

        return request.getScheme() + "://" +
                request.getServerName() + ":" +
                request.getServerPort() +
                request.getContextPath();
    }

    @GetMapping("/hierarchy")
    public ResponseEntity<List<String>> getHierarchy() {
        return ResponseEntity.ok(List.of(
                RegionTypes.WORLD.getName(),
                RegionTypes.CONTINENT.getName(),
                RegionTypes.CONTINENT_REGION.getName(),
                RegionTypes.COUNTRY.getName(),
                RegionTypes.DISTRICT.getName()
        ));
    }

    private Pair<List<RegionDto>, Long> getRegionsWithType(PaginationRequest pagination) {
        List<RegionDto> regions;
        long total;
        if (pagination.getRegionType() != null) {
            total = regionService.getTotalRegionsByType(pagination.getRegionType());
            regions = regionService.getRegionsByType(
                    pagination.getRegionType(),
                    pagination.page != null ? pagination.page : 0,
                    pagination.size != null ? pagination.size : (int) total
            );
        } else {
            total = regionService.getTotalRegions();
            regions = regionService.getRegions(
                    pagination.page != null ? pagination.page : 0,
                    pagination.size != null ? pagination.size : (int) total,
                    pagination.sortBy,
                    pagination.sortDir
            );
        }

        return new Pair<>(regions, total);
    }

    @GetMapping("/simplified")
    public ResponseEntity<List<Pair<IRI, String>>> getRegionsSimplified(@ModelAttribute PaginationRequest pagination) {
        PaginationRequest noPagination = new PaginationRequest(
                null, null, null, null, pagination.regionType(), true
        );
        Pair<List<RegionDto>, Long> output = getRegionsWithType(noPagination);
        return ResponseEntity.ok(output.getValue0().stream().map(i ->
                new Pair<>(i.getId(), i.getName())).toList());
    }

    private String generateJsonPath(RegionDto dto) {
        return getBaseUrl() + "/maps/" +
                dto.getType().getName().toLowerCase() + "/" + dto.getName() + ".json";
    }

    @GetMapping
    public ResponseEntity<ResponsePaginated<List<ResponseRegionDto>>> getRegions(@ModelAttribute PaginationRequest pagination) {
        Pair<List<RegionDto>, Long> output = getRegionsWithType(pagination);
        List<ResponseRegionDto> regionsDto = output.getValue0().stream().map(r ->
                new ResponseRegionDto(r, generateJsonPath(r))).toList();

        var responsePaginated = new ResponsePaginated<>(
                regionsDto, new ResponsePaginated.Pagination(pagination.page, output.getValue1(), pagination.size)
        );

        return ResponseEntity.ok(responsePaginated);
    }



    @GetMapping(value = "/{regionId}")
    public ResponseEntity<Optional<ResponseRegionDto>> getRegionDto(@PathVariable String regionId) {
        Optional<RegionDto> dto = regionService.getRegion(regionId);
        return ResponseEntity.ok(dto.map(i -> new ResponseRegionDto(i, generateJsonPath(i))));
    }
}
