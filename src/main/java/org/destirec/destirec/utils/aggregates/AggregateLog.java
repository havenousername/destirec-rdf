package org.destirec.destirec.utils.aggregates;

import java.util.function.Function;

public class AggregateLog implements Function<String, Double> {
    @Override
    public Double apply(String featureQuality) {
        return Math.log(new AggregateCount().apply(featureQuality)) + 1;
    }
}
