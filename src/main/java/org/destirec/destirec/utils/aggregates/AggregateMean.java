package org.destirec.destirec.utils.aggregates;

import java.util.Arrays;
import java.util.function.Function;

public class AggregateMean implements Function<String, Double> {
    private Double aggregateFeatureQuality(String featureQuality) {
        return Arrays.stream(featureQuality.split(","))
                .toList()
                .stream()
                .map(Integer::parseInt)
                .mapToDouble(i -> i)
                .average().orElse(0);
    }

    @Override
    public Double apply(String s) {
        return aggregateFeatureQuality(s);
    }
}
