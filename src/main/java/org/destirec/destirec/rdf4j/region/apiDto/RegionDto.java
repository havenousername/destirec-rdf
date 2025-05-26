package org.destirec.destirec.rdf4j.region.apiDto;

import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames;

import java.time.Month;
import java.util.List;
import java.util.Map;

public interface RegionDto {
    Map<Month, Integer> getMonths();
    List<Integer> getCost();
    Map<RegionFeatureNames.Individuals.RegionFeature, Integer> getFeatures();
}
