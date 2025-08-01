package org.destirec.destirec.rdf4j.knowledgeGraph;

import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames.Individuals.RegionFeature;
import org.javatuples.Pair;

import java.util.*;

public class AntColonyOptimizer {
    private final List<POIClass> poiClasses;
    private final ACOHyperparameters hyperparameters;
    private final double[] pheromones;
    private final double[] heuristicScores;
    private final int targetSize;
    private final boolean isSingleFeatureDataset;

    public AntColonyOptimizer(List<POIClass> poiClasses, ACOHyperparameters hyperparameters) {
        this.poiClasses = poiClasses;
        this.hyperparameters = hyperparameters;
        this.pheromones = new double[poiClasses.size()];
        this.heuristicScores = new double[poiClasses.size()];
        for (int i = 0; i < pheromones.length; i++) {
            pheromones[i] = 1.0;
            heuristicScores[i] = poiClasses.get(i).getHeuristicScore();
        }

        isSingleFeatureDataset = poiClasses.stream()
                .map(POIClass::getFeature)
                .distinct()
                .count() == 1;


        targetSize = (int)Math.min(hyperparameters.getTrueSelectionSize(), poiClasses.size());
    }

    public POIClass[] optimize() {
        if (poiClasses.isEmpty() || hyperparameters.getTrueSelectionSize() < 2) {
            return poiClasses.toArray(new POIClass[0]);
        }
        if (poiClasses.size() <= hyperparameters.getTrueSelectionSize()) {
            return poiClasses.toArray(new POIClass[0]);
        }

        POIClass[] bestSolution = new POIClass[targetSize];
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < hyperparameters.getNumberOfIterations(); i++) {
            boolean isNewBestScore = false;
            double bestScoreThisIter = Double.NEGATIVE_INFINITY;
            boolean[] available = new boolean[poiClasses.size()];

            for (int ant = 0; ant < hyperparameters.getNumberOfAnts(); ant++) {
                Pair<POIClass[], boolean[]> solution = constructSolution();
                Pair<Map<RegionFeature, Integer>, Integer> featureCounts = calculateFeatureCounts(solution.getValue0());
                double currentScore = evaluateSolution(solution.getValue0(), solution.getValue1(), featureCounts);

                if (currentScore > bestScoreThisIter) {
                    bestScoreThisIter = currentScore;
                    isNewBestScore = true;
                    available = solution.getValue1();
                }

                // update best solution
                if (currentScore > bestScore) {
                    bestScore = currentScore;
                    bestSolution = Arrays.copyOf(
                            solution.getValue0(),
                            solution.getValue0().length,
                            POIClass[].class
                    );
                }
            }

            if (isNewBestScore) {
                updatePheromones(available, bestScoreThisIter);
            }
        }

        return bestSolution;
    }

    private double calculateFeatureEntropy(Pair<Map<RegionFeature, Integer>, Integer> featureCounts) {
        if (featureCounts.getValue1() == 0) return 0.0;

        double entropy = 0.0;
        double logBase = Math.log(featureCounts.getValue0().size());
        for (int count : featureCounts.getValue0().values()) {
            double proportion = (double) count / featureCounts.getValue1();
            entropy -= proportion * Math.log(proportion);
        }

        return entropy / logBase;
    }

    private static Pair<Map<RegionFeature, Integer>, Integer> calculateFeatureCounts(POIClass[] poiClasses) {
        Map<RegionFeature, Integer> featureCounts = new HashMap<>();
        int totalCount = 0;
        for (POIClass poiClass : poiClasses) {
            if (poiClass.getFeature() == null) continue;
            featureCounts.merge(poiClass.getFeature(), 1, Integer::sum);
            totalCount++;
        }

        return new Pair<>(featureCounts, totalCount);
    }

    private Pair<POIClass[], boolean[]> constructSolution() {
        POIClass[] selected = new POIClass[targetSize];
        boolean[] available = new boolean[poiClasses.size()];
        Arrays.fill(available, true);

        int unavailableSize = 0;

        double numberToSelect = Math.min(hyperparameters.getTrueSelectionSize(), targetSize);

        if (numberToSelect == 0) {
            return new Pair<>(new POIClass[0], available) ;
        }

        while (unavailableSize < targetSize) {
            int selectedIndex = selectNextPOI(available);
            if (selectedIndex == -1) {
                break;
            }
            if (!available[selectedIndex]) {
                continue;
            }
            POIClass chosenPOI = poiClasses.get(selectedIndex);
            selected[unavailableSize++] = chosenPOI;
            available[selectedIndex] = false;
        }

        return new Pair<>(selected, available);
    }

    private int selectNextPOI(boolean[] available) {
        double[] selectionProbabilities = new double[available.length];
        double probabilitySum = 0.0d;

        for (int i = 0; i < available.length; i++) {
            if (!available[i]) {
                selectionProbabilities[i] = 0.0d;
            } else {
                double pheromone = Math.max(pheromones[i], 0.00001);
                double heuristics = Math.max(heuristicScores[i], 0.00001);
                double probability = Math.pow(pheromone, hyperparameters.getAlpha()) * Math.pow(heuristics, hyperparameters.getBeta());
                selectionProbabilities[i] = probability;
                probabilitySum += probability;
            }
        }

        if (probabilitySum <= 0.0d) {
            return -1;
        }

        double random = Math.random() * probabilitySum;
        double cumulative = 0.0d;

        for (int i = 0; i < available.length; i++) {
            if (!available[i]) {
                continue;
            }
            cumulative += selectionProbabilities[i];
            if (cumulative >= random) {
                return i;
            }
        }

        // Fallback: return first available
        for (int i = 0; i < available.length; i++) {
            if (available[i]) return i;
        }


        // fallback solution
        return -1;
    }


    private void updatePheromones(boolean[] available, double scoreThisIter) {
        for (int i = 0; i < pheromones.length; i++) {
            pheromones[i] = pheromones[i] * (1 - hyperparameters.getEvaporationFactor());
        }

        double pheromoneToAdd = hyperparameters.getPheromoneBoost() * scoreThisIter;
        if (pheromoneToAdd <= 0) {
            return;
        }
        for (int i = 0; i < available.length; i++) {
            // if in use then update pheromons
            if (available[i]) {
                continue;
            }
            pheromones[i] += pheromoneToAdd;
        }
    }

    private double evaluateSolution(POIClass[] solution, boolean[] available, Pair<Map<RegionFeature, Integer>, Integer> featureCounts) {
        if (solution == null || solution.length == 0) {
            return 0.0;
        }
        double scoreSum = 0.0;
        for (int i = 0; i < poiClasses.size(); i++) {
            if (available[i]) {
                continue;
            }
            scoreSum += this.heuristicScores[i];
        }

        double distributionScore = 0.0;
        if (isSingleFeatureDataset) {
            distributionScore = 1;
        } else {
            double entropyScore = calculateFeatureEntropy(featureCounts);
            double coverageScore = ((double) featureCounts.getValue0().size()) / RegionFeature.values().length;

            distributionScore = (0.5 * entropyScore + 0.5 * coverageScore);
        }


        return scoreSum + distributionScore * hyperparameters.getPheromoneBoost();
    }
}
