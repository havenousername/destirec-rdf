package org.destirec.destirec.rdf4j.region.apiDto;

import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames;
import org.javatuples.Pair;

public interface POIRegion {
    Pair<RegionFeatureNames.Individuals.RegionFeature, Integer> getFeatureScore();
}
