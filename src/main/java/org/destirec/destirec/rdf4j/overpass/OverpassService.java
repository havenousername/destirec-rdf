package org.destirec.destirec.rdf4j.overpass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

import static java.net.URI.create;

@Service
public class OverpassService {
    RestClient client = RestClient.create();
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public OverpassService() {
    }

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

    public String runQueryDistrict(String country, String districtIso) {
        return runQuery(getCountryDistrict(country, districtIso));
    }

    public String runQueryCountry(String country) {
        return runQuery(getCountry(country));
    }

    public String runQuerySuperRegion(List<String> country) {
        return runQuery(getSuperRegion(country));
    }

    public String runWikimediaQuery(String uri) {
        return client
                .get()
                .uri(create(uri))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((_, response) -> {
                    logger.warn("wikimedia resource is not accessible from wikimedia: {}. Error{}", uri, response.getBody());
                    throw new RuntimeException("Cannot get required query: " + uri + ". Error" + response.getBody());
                }))
                .body(String.class);
    }
}
