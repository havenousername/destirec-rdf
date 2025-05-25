package org.destirec.destirec.rdf4j.region.apiDto;

import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames;

import java.time.Month;
import java.util.List;
import java.util.Map;

public record ExternalRegionDto(
        String id,
        String u_name,

        String Region,

        String sourceIRI,
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
    public Map<RegionFeatureNames.Individuals.RegionFeature, Integer> getFeatures() {
        return Map.ofEntries(
                Map.entry(RegionFeatureNames.Individuals.RegionFeature.SAFETY, safety),
                Map.entry(RegionFeatureNames.Individuals.RegionFeature.NATURE, nature),
                Map.entry(RegionFeatureNames.Individuals.RegionFeature.HIKING, hiking),
                Map.entry(RegionFeatureNames.Individuals.RegionFeature.BEACH, beach),
                Map.entry(RegionFeatureNames.Individuals.RegionFeature.WATERSPORTS, watersports),
                Map.entry(RegionFeatureNames.Individuals.RegionFeature.ENTERTAINMENT, entertainment),
                Map.entry(RegionFeatureNames.Individuals.RegionFeature.WINTERSPORTS, wintersports),
                Map.entry(RegionFeatureNames.Individuals.RegionFeature.CULTURE, culture),
                Map.entry(RegionFeatureNames.Individuals.RegionFeature.CULINARY, culinary),
                Map.entry(RegionFeatureNames.Individuals.RegionFeature.ARCHITECTURE, architecture),
                Map.entry(RegionFeatureNames.Individuals.RegionFeature.SHOPPING, shopping)
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
