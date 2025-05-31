package org.destirec.destirec.rdf4j.overpass;

import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.destirec.destirec.rdf4j.overpass.OverpassService.MAP_JSON_DIR;


@RestController
@RequestMapping("/maps")
public class OverpassMapsController {
    private final Path uploadDir = Paths.get(MAP_JSON_DIR);


    @GetMapping("/{regionType}/{filename:.+}")
    public ResponseEntity<Resource> getOverpassMap(@PathVariable String regionType, @PathVariable String filename) {
        RegionNames.Individuals.RegionTypes type;
        try {
            type = RegionNames.Individuals.RegionTypes.fromString(regionType);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
        Path file = uploadDir.resolve(type.getName().toLowerCase()).resolve(filename).normalize();
        System.out.println(file.toAbsolutePath());
        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
