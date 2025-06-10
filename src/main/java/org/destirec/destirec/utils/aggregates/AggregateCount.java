package org.destirec.destirec.utils.aggregates;

import java.util.Arrays;
import java.util.function.Function;

public class AggregateCount implements Function<String, Double> {
    @Override
    public Double apply(String featureQuality) {
        return (double)Arrays.stream(featureQuality.split(","))
                .toList()
                .size();
    }
}
