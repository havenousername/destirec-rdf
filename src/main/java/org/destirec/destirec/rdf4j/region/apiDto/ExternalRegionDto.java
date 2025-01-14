package org.destirec.destirec.rdf4j.region.apiDto;

import java.time.Month;
import java.util.List;
import java.util.Map;

public record ExternalRegionDto(
        String id,
        String u_name,

        String Region,
        int costPerWeek,

        int safety,
        int nature,

        int hiking,

        int beach,

        int watersports,

        int entertainment,

        int wintersports,


        int culture,


        int culinary,


        int architecture,


        int shopping,

        int jan,

        int feb,

        int mar,

        int apr,

        int may,

        int jun,
        int jul,
        int aug,
        int sep,
        int oct,
        int nov,

        int dec,

        int budgetLevel,

        String graphId,

        String parentRegion
) {
    public Map<String, Integer> getFeatures() {
        return Map.ofEntries(
                Map.entry("safety", safety),
                Map.entry("nature", nature),
                Map.entry("hiking", hiking),
                Map.entry("beach", beach),
                Map.entry("watersports", watersports),
                Map.entry("entertainment", entertainment),
                Map.entry("wintersports", wintersports),
                Map.entry("culture", culture),
                Map.entry("culinary", culinary),
                Map.entry("architecture", architecture),
                Map.entry("shopping", shopping)
        );
    }

    public Map<Month, Integer> getMonths() {
        return Map.ofEntries(
                Map.entry(Month.JANUARY, jan),
                Map.entry(Month.FEBRUARY, feb),
                Map.entry(Month.MARCH, mar),
                Map.entry(Month.APRIL, apr),
                Map.entry(Month.MAY, may),
                Map.entry(Month.JUNE, jun),
                Map.entry(Month.JULY, jul),
                Map.entry(Month.AUGUST, aug),
                Map.entry(Month.SEPTEMBER, sep),
                Map.entry(Month.OCTOBER, oct),
                Map.entry(Month.NOVEMBER, nov)
        );
    }

    public List<Integer> getCost() {
        return List.of(
                costPerWeek,
                budgetLevel
        );
    }

    public String getName() {
        return Region;
    }
}
