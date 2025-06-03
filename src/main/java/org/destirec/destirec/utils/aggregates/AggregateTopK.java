package org.destirec.destirec.utils.aggregates;


import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

public class AggregateTopK implements Function<String, Double> {
    private final int k;
    public AggregateTopK(int k) {
        this.k = k;
    }

    @Override
    public Double apply(String featureQuality) {
        return Arrays.stream(featureQuality.split(","))
                .toList()
                .stream()
                .map(Double::parseDouble)
                .sorted(Comparator.reverseOrder())
                .limit(k)
                .mapToDouble(i -> i)
                .average()
                .orElse(0.0);
    }
}
