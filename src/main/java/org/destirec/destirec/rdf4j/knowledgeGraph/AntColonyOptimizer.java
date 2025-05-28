package org.destirec.destirec.rdf4j.knowledgeGraph;

import java.util.*;

public class AntColonyOptimizer {
    private final List<POIClass> poiClasses;
    private final ACOHyperparameters hyperparameters;
    private final Map<String, Double> pheromones;

    public AntColonyOptimizer(List<POIClass> poiClasses, ACOHyperparameters hyperparameters) {
        this.poiClasses = poiClasses;
        this.hyperparameters = hyperparameters;
        this.pheromones = new HashMap<>();
        for (POIClass poiClass : poiClasses) {
            pheromones.put(poiClass.getId(), 1.0);
        }
    }

    public List<POIClass> optimize() {
        if (poiClasses.isEmpty() || hyperparameters.getSelectionSize() < 1) {
            return new ArrayList<>();
        }
        if (poiClasses.size() <= hyperparameters.getSelectionSize()) {
            return new ArrayList<>(poiClasses);
        }

        List<POIClass> bestSolution = new ArrayList<>();
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < hyperparameters.getNumberOfIterations(); i++) {
            Map<List<POIClass>, Double> antSolutions = new HashMap<>();

            for (int ant = 0; ant < hyperparameters.getNumberOfAnts(); ant++) {
                List<POIClass> solution = constructSolution();
                if (antSolutions.isEmpty()) {
                    double currentScore = evaluateSolution(solution);
                    antSolutions.put(solution, currentScore);

                    // update best solution
                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        bestSolution = new ArrayList<>(solution);
                    }
                }
            }

            if (!antSolutions.isEmpty()) {
                updatePheromones(antSolutions);
            }
        }

        return bestSolution;
    }

    private List<POIClass> constructSolution() {
        List<POIClass> selected = new ArrayList<>();
        List<POIClass> available = new ArrayList<>(poiClasses);

        double numberToSelect = Math.min(hyperparameters.getSelectionSize(), available.size());

        if (numberToSelect == 0) {
            return selected;
        }


        for (int i = 0; i < numberToSelect; i++) {
            if (available.isEmpty()) {
                break;
            }
            int selectedIndex = selectNextPOI(available);

            POIClass chosenPOI = available.get(selectedIndex);
            selected.add(chosenPOI);
            available.remove(selectedIndex);
        }

        return selected;
    }

    private int selectNextPOI(List<POIClass> available) {
        if (available.isEmpty()) {
            throw new IllegalArgumentException("Cannot select next POI from empty list");
        }

        List<Double> selectionProbabilities = new ArrayList<>();
        double probabilitySum = 0.0d;

        for (POIClass poiClass : available) {
            double pheromone = pheromones.getOrDefault(poiClass.getId(), 0.00001);
            double heuristics = poiClass.getHeuristicScore();
            if (heuristics <= 0) {
                // always have some positive heuristic score (even very small)
                heuristics = 0.00001;
            }

            double probability = Math.pow(pheromone, hyperparameters.getAlpha()) * Math.pow(heuristics, hyperparameters.getBeta());
            selectionProbabilities.add(probability);
            probabilitySum += probability;
        }

        double random = Math.random() * probabilitySum;
        double cumulative = 0.0d;

        for (int i = 0; i < available.size(); i++) {
            cumulative += selectionProbabilities.get(i);
            if (cumulative >= random) {
                return i;
            }
        }

        // fallback solution
        return available.size() - 1;
    }


    private void updatePheromones(Map<List<POIClass>, Double> solutionsThisIter) {
        pheromones.replaceAll((i, _) -> pheromones.get(i) * (1 - hyperparameters.getEvaporationFactor()));

        for (Map.Entry<List<POIClass>, Double> entry : solutionsThisIter.entrySet()) {
            List<POIClass> solution = entry.getKey();
            double score = entry.getValue();

            double pheromoneToAdd = hyperparameters.getPheromoneBoost() * score;
            if (pheromoneToAdd <= 0) {
                continue;
            }
            for (POIClass poi : solution) {
                pheromones.compute(poi.getId(), (_, currentVal) ->
                        (currentVal == null ? 0 : currentVal) + pheromoneToAdd
                );
            }
        }
    }

    private double evaluateSolution(List<POIClass> solution) {
        if (solution == null || solution.isEmpty()) {
            return 0.0;
        }
        double scoreSum = solution
                .stream()
                .mapToDouble(POIClass::getHeuristicScore)
                .sum();
        long diversity = solution
                .stream()
                .map(POIClass::getFeature)
                .distinct()
                .count();
        return scoreSum + diversity * hyperparameters.getPheromoneBoost();
    }
}
