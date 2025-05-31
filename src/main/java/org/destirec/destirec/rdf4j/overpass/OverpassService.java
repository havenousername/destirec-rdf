package org.destirec.destirec.rdf4j.overpass;

import org.destirec.destirec.utils.JsonValidator;
import org.destirec.destirec.utils.rdfDictionary.RegionNames;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.net.URI.create;

@Service
public class OverpassService {
    private final RestClient client = RestClient.create();
    protected Logger logger = LoggerFactory.getLogger(getClass());
    public static final String MAP_JSON_DIR = "./maps";

    public OverpassService() {}

    public static String getSuperRegion(List<String> countriesIso) {
        return """
            [out:json][timeout:180];
                    relation["boundary"="administrative"]["admin_level"="2"]["ISO3166-1"~"^(%s)$"];
                    out geom;
        """.formatted(String.join("|", countriesIso));
    }

    public static String getCountry(String country) {
        return """
            [out:json][timeout:180];
                    relation["boundary"="administrative"]["admin_level"="2"]["ISO3166-1"="%s"];
                    out geom;
        """.formatted(country);
    }

    public static String getCountryDistrict(String country, String districtIso) {
        return """
            [out:json][timeout:180];
            area["ISO3166-1"="%s"][admin_level=2];   // Country area
            (
              relation["admin_level"~"^(3|4|5|7|8)$"]
              ["ISO3166-1"="%s"]
              (area);
            );
            out geom;
        """.formatted(country, districtIso);
    }

    public String runQuery(String query) {
        String url = "https://overpass-api.de/api/interpreter";
        return client
                .post()
                .uri(create(url))
                .body(query)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request1, response1) -> {
                    throw new RuntimeException("Cannot get required query: " + query + ". Error" + response1.getBody());
                })
                .body(String.class);
    }

    public String runQueryDistrict(String countryIso, String districtIso) {
        return runQuery(getCountryDistrict(countryIso, districtIso));
    }

    public String runQueryCountry(String country) {
        return runQuery(getCountry(country));
    }

    public String runQuerySuperRegion(List<String> country) {
        return runQuery(getSuperRegion(country));
    }


    // https://commons.wikimedia.org/wiki/Special:PageData/main/South+America.map?action=raw
    public String runWikimediaQuery(String uri) {
        Pattern uriPattern = Pattern.compile("^https?://[^/]+/data/[^/]+/Data:(.+).map");
        String region = uriPattern.matcher(uri)
                .results()
                .map(mr -> mr.group(1))
                .toList()
                .getFirst();

        String wikiUri = "https://commons.wikimedia.org/w/index.php?title=Data:" + region + ".map";
        URI url = UriComponentsBuilder
                        .fromUriString(wikiUri)
                        .queryParam("action", "raw")
                        .build(true)
                        .toUri();
        String result = client
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String body = response.getBody().toString();
                    logger.warn("Wikimedia resource is not accessible from wikimedia: {}. Error: {}", uri, body);
                    throw new RuntimeException("Cannot get required query: " + uri + ". Error: " + body);
                })
                .body(String.class);

        if (!JsonValidator.isValidJsonObject(result)) {
            return null;
        }
        return result;
    }

    public void saveRegionGeoJson(String fileContent, RegionNames.Individuals.RegionTypes type, String regionId) {
        saveRegionGeoJson(fileContent, type, regionId, false);
    }

    public void saveRegionGeoJson(String fileContent, RegionNames.Individuals.RegionTypes type, String regionId, boolean isMapJson) {
        try {
            Path dir = Paths.get(MAP_JSON_DIR);
            Files.createDirectories(dir);

            Path location = dir.resolve(type.name().toLowerCase());
            Files.createDirectories(location);

            File jsonFile = location.resolve(regionId + ".json").toFile();
            String mapJson = isMapJson ? fileContent : transformToGeoJson(fileContent);
            if (mapJson != null && !mapJson.isBlank()) {
                Files.writeString(jsonFile.toPath(), mapJson, StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            logger.error("Failed to write optimized POIs to file", exception);
        }
    }

    public boolean regionGeoJsonExists(RegionNames.Individuals.RegionTypes type, String regionId) {
        Path dir = Paths.get(MAP_JSON_DIR);
        Path location = dir.resolve(type.name().toLowerCase());
        Path jsonFilePath = location.resolve(regionId + ".json");

        return Files.exists(jsonFilePath) && Files.isRegularFile(jsonFilePath);
    }

    private String transformToGeoJson(String fileContent) {
        if (fileContent == null || fileContent.isBlank()) {
            return fileContent;
        }
        try {
            URI jsFileUri = Objects.requireNonNull(getClass().getClassLoader().getResource("scripts/osmtogeojson.js")).toURI();
            Path jsFile = Paths.get(jsFileUri);
            String osmGeoJsonJS = Files.readString(jsFile);

            try (Context context = Context
                    .newBuilder("js")
                    .allowAllAccess(true)
                    .build()
            ) {
                context.eval("js", osmGeoJsonJS);
                context.getBindings("js").putMember("osmGeoJson", fileContent);
                Value geoJson = context.eval("js", "JSON.stringify(osmtogeojson(JSON.parse(osmGeoJson)));");
                return geoJson.asString();
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
