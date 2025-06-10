package org.destirec.destirec.rdf4j.knowledgeGraph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents hyperparameters for Ant Colony Optimization (ACO) algorithm.
 * These parameters control the behavior and performance of the ACO algorithm
 * in finding optimal paths through the knowledge graph.
 */
@AllArgsConstructor
@Getter
@Setter
public class ACOHyperparameters {
    /**
     * Number of ants used in the colony for path finding
     */
    private int numberOfAnts;
    /**
     * Maximum number of iterations for the algorithm to run
     */
    private int numberOfIterations;
    /**
     * Influence of pheromone trails on ant path selection
     */
    private double alpha;
    /**
     * Influence of heuristic information on ant path selection
     */
    private double beta;
    /**
     * Rate at which pheromone trails evaporate between iterations
     */
    private double evaporationFactor;
    /**
     * Multiplier for pheromone deposition on paths
     */
    private double pheromoneBoost;
    /**
     * Percentage of the best paths to select for pheromone update
     */
    private double selectionSize;


    private double maxSelectionSize;

    public double getTrueSelectionSize() {
        return Math.min(selectionSize, maxSelectionSize);
    }

    public static ACOHyperparameters getDefault() {
        return new ACOHyperparameters(
                50,
                100,
                1.0,
                2.0,
                0.5,
                1.0,
                40,
                30
        );
    }
}
