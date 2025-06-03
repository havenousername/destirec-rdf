package org.destirec.destirec.rdf4j.user.apiDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ExternalPreference {
    private final String userId;
    private final Map<String, List<Object>> features;
    private static Logger logger = LoggerFactory.getLogger("ExternalPreference");

    public Map<RegionFeatureNames.Individuals.RegionFeature, Pair<Integer, Boolean>> getFeatures() {
        Map<RegionFeatureNames.Individuals.RegionFeature, Pair<Integer, Boolean>> featureTupleMap = new HashMap<>();

        features.forEach((feature, value) -> {
            try {
                var featureKey = RegionFeatureNames.Individuals.RegionFeature.fromString(feature);
                if (value.size() != 2) {
                    throw new RuntimeException("Tuple have size that doesnt equal to 2");
                }
                Pair<Integer, Boolean> featureValue = new Pair<>(
                        Integer.parseInt(value.getFirst().toString()),
                        Boolean.parseBoolean(value.getLast().toString())
                );

                featureTupleMap.put(featureKey, featureValue);
            } catch (IllegalArgumentException exception) {
                logger.error("Could not parse feature " + feature + ". Key is not presented in allowed enum set");
                throw exception;
            } catch (RuntimeException exception) {
                logger.warn(exception.getMessage());
                throw exception;
            } catch (Exception e) {
                logger.warn("Couldn't parse the feature " + feature + ". Skipping it...");
                throw e;
            }
        });

        return featureTupleMap;
    }
}
