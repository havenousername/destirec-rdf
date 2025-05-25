package org.destirec.destirec.rdf4j.knowledgeGraph;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ACOHyperparameters {
    private int numberOfAnts;
    private int numberOfIterations;
    private double alpha;
    private double beta;
    private double evaporationFactor;
    private double pheromoneBoost;
    private double selectionSize;


    public static ACOHyperparameters getDefault() {
        return new ACOHyperparameters(
                50,
                100,
                1.0,
                2.0,
                0.5,
                1.0,
                40
        );
    }
}
