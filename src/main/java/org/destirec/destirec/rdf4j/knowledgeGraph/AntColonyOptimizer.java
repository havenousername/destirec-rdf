package org.destirec.destirec.rdf4j.knowledgeGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<POIClass> bestSolution = new ArrayList<>();
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < hyperparameters.getNumberOfAnts(); i++) {
            List<List<POIClass>> allSolutions = new ArrayList<>();

            for (int ant = 0; ant < hyperparameters.getNumberOfIterations(); ant++) {
                List<POIClass> selected = constructSolution();
                allSolutions.add(selected);
            }

            updatePheromones(allSolutions);


            for (List<POIClass> solution : allSolutions) {
                double score = evaluateSolution(solution);
                if (score > bestScore) {
                    bestScore = score;
                    bestSolution = solution;
                }
            }
        }

        return bestSolution;
    }

    private List<POIClass> constructSolution() {
        List<POIClass> available = new ArrayList<>(poiClasses);
        List<POIClass> selected = new ArrayList<>();

        for (int i = 0; i < hyperparameters.getNumberOfIterations(); i++) {
            POIClass next = selectNextPOI(available, selected);
            selected.add(next);
            available.remove(next);
        }

        return selected;
    }

    private POIClass selectNextPOI(List<POIClass> available, List<POIClass> selected) {
        double sum = 0.0d;

        Map<POIClass, Double> probabilities = new HashMap<>();
        for (POIClass poiClass : available) {
            double pheromone = pheromones.get(poiClass.getId());
            double heuristics = poiClass.getHeuristicScore();
            double score = Math.pow(pheromone, hyperparameters.getAlpha()) * Math.pow(heuristics, hyperparameters.getBeta());
            probabilities.put(poiClass, score);
            sum += score;
        }

        double random = Math.random() * sum;
        double cumulative = 0.0d;

        for (Map.Entry<POIClass, Double> entry : probabilities.entrySet()) {
            cumulative += entry.getValue();

            if (cumulative >= random) {
                return entry.getKey();
            }
        }

        // fallback solution
        return available.getFirst();
    }


    private void updatePheromones(List<List<POIClass>> allSolutions) {
        pheromones.replaceAll((i, _) -> pheromones.get(i) * (1 - hyperparameters.getEvaporationFactor()));
        for (List<POIClass> solution : allSolutions) {
            double score = evaluateSolution(solution);
            for (POIClass poiClass : solution) {
                pheromones.put(poiClass.getId(), pheromones.get(poiClass.getId()) + hyperparameters.getPheromoneBoost() * score);
            }
        }
    }

    private double evaluateSolution(List<POIClass> solution) {
        double score = solution.stream().mapToDouble(POIClass::getHeuristicScore).sum();
        long diversity = solution.stream().map(POIClass::getFeature).distinct().count();
        return score + diversity * 2.0;
    }
}
