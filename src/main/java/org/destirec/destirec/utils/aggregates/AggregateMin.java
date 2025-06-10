package org.destirec.destirec.utils.aggregates;

import java.util.Arrays;
import java.util.function.Function;

public class AggregateMin implements Function<String, Double> {
    @Override
    public Double apply(String featureQuality) {
        return  Arrays.stream(featureQuality.split(","))
                .toList()
                .stream()
                .map(Integer::parseInt)
                .mapToDouble(i -> i)
                .min()
                .orElse(0);
    }
}
