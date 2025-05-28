package org.destirec.destirec.rdf4j.user.apiDto;

import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ExternalUserDto(
        String id,
        String name,
        String username,
        String email,
        String occupation,

        Map<String, List<Object>> features,

        Map<String, List<Object>> months,

        List<Object> cost

) {
    static Logger logger = LoggerFactory.getLogger("ExternalUserDto");
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

    public Map<Month, Pair<Integer, Boolean>> getMonths() {
        Map<Month, Pair<Integer, Boolean>> monthsMap = new HashMap<>();

        months.forEach((month, value) -> {
            try {
                var monthCls = Month.of(Integer.parseInt(month));
                if (value.size() != 2) {
                    throw new RuntimeException("Tuple have size that doesnt equal to 2");
                }
                Pair<Integer, Boolean> featureValue = new Pair<>(
                        Integer.parseInt(value.getFirst().toString()),
                        Boolean.parseBoolean(value.getLast().toString())
                );

                monthsMap.put(monthCls, featureValue);
            } catch (DateTimeException exception) {
                logger.error("Could not parse month " + month + ". Key is not presented in allowed enum set");
                throw exception;
            } catch (RuntimeException exception) {
                logger.error(exception.getMessage());
                throw exception;
            } catch (Exception e) {
                logger.error("Couldn't parse the feature " + month + ". Skipping it...");
                throw e;
            }
        });

        return monthsMap;
    }

    public Triplet<Integer, Integer, Boolean> getCost() {
        try {
            if (cost.size() != 3) {
                throw new RuntimeException("Tuple have size that doesnt equal to 2");
            }
            return Triplet.with(
                    Integer.parseInt(cost.getFirst().toString()),
                    Integer.parseInt(cost.get(1).toString()),
                    Boolean.parseBoolean(cost.getLast().toString())
            );
        } catch (DateTimeException exception) {
            logger.error("Could not parse month " + cost + ". Key is not presented in allowed enum set");
            throw exception;
        } catch (RuntimeException exception) {
            logger.error(exception.getMessage());
            throw exception;
        } catch (Exception e) {
            logger.error("Couldn't parse the feature " + cost + ". Skipping it...");
            throw e;
        }
    }


    public boolean hasPreferences() {
        return features() != null && months() != null && cost != null;
    }
}
