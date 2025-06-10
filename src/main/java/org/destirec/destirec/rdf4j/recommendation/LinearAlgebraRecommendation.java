package org.destirec.destirec.rdf4j.recommendation;

import org.apache.commons.math3.linear.*;
import org.destirec.destirec.rdf4j.poi.POIDao;
import org.destirec.destirec.rdf4j.preferences.PreferenceDto;
import org.destirec.destirec.rdf4j.region.RegionDao;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.region.feature.FeatureDto;
import org.destirec.destirec.utils.rdfDictionary.RegionFeatureNames.Individuals.RegionFeature;
import org.eclipse.rdf4j.model.IRI;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

// TODO: Fix the algorithm
public class LinearAlgebraRecommendation {
    private final List<RecommendationEntity> recommendationEntities;
    private final RegionDao regionDao;
    private final POIDao poiDao;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private RealMatrix rPOIMatrix;
    private RealMatrix rFQMatrix;


    public LinearAlgebraRecommendation(List<RecommendationEntity> recommendationEntities, RegionDao dao, POIDao poiDao) {
        this.recommendationEntities = recommendationEntities;
        this.regionDao = dao;
        this.poiDao = poiDao;
    }

    // We need RPOI matrix (Region x POI matrix)
    public void createRPOIMatrix() {
        int numRegions = recommendationEntities.size();
        List<RegionDto> regions = recommendationEntities.stream().map(RecommendationEntity::getRegion).distinct().toList();

        Map<IRI, List<Pair<IRI, Integer>>> regionToPoi = new HashMap<>();
        int highestRegion = 0;
        Set<IRI> poiIndices = new LinkedHashSet<>();
        for (RegionDto region : regions) {
            var poisWithRegionCount = poiDao.listAllByRegionOnlyPOI(region.getId());
            for (var poiWithRegionCount : poisWithRegionCount) {
                IRI poiId = poiWithRegionCount.getValue0();
                int count = poiWithRegionCount.getValue2();

                regionToPoi.putIfAbsent(region.getId(), new ArrayList<>());
                regionToPoi.get(region.getId()).add(new Pair<>(poiId, count));

                poiIndices.add(poiId);
                highestRegion = Math.max(highestRegion, count);
            }
        }

        int numberOfPOIs = poiIndices.size();
        RealMatrix poiToRMatrix = new Array2DRowRealMatrix(numRegions, numberOfPOIs);
        List<IRI> poiList = new ArrayList<>(poiIndices);

        for (int regionIdx = 0; regionIdx < numRegions; regionIdx++) {
            IRI regionId = regions.get(regionIdx).getId();
            List<Pair<IRI, Integer>> regionPois = regionToPoi.getOrDefault(regionId, Collections.emptyList());

            Map<IRI, Integer> poiToCountMap = regionPois.stream().collect(Collectors.toMap(Pair::getValue0, Pair::getValue1));

            int finalHighestRegion = highestRegion;
            int finalRegionIdx = regionIdx;
            poiToCountMap.forEach((poi, count) -> {
                int indexOfPoi = poiList.indexOf(poi);
                poiToRMatrix.setEntry(finalRegionIdx, indexOfPoi,   1d / (Math.abs(finalHighestRegion - count) + 1));
            });
        }
        rPOIMatrix = poiToRMatrix;
    }


    private double aggregateFeatureQuality(String featureQuality, RegionFeature regionFeature) {
        return regionFeature.getScoreFunction().apply(featureQuality);
    }

    // We need RFQ matrix (Region x Feature matrix)
    public void createRFQMatrix() {
        int numRegions = recommendationEntities.size();
        int numFeatures = RegionFeature.values().length;
        List<RegionDto> regions = recommendationEntities.stream().map(RecommendationEntity::getRegion).distinct().toList();

        RealMatrix frqMatrix = new Array2DRowRealMatrix(numRegions, numFeatures);
        for (int i = 0; i < numRegions; i++) {
            var regionScores = regionDao.getCountryScores(regions.get(i).getId());
            for (var regionScore : regionScores) {
                try {
                    RegionFeature regionFeature = RegionFeature.fromIri(regionScore.getValue2());
                    double score = aggregateFeatureQuality(regionScore.getValue1(), regionFeature);
                    if (!regionScore.getValue0().equals(regions.get(i).getId())) {
                        throw new RuntimeException("Region feature should be presented in the" +
                                " same region. Expected " + regions.get(i).getId() + ", but got " + regionScore.getValue0());
                    }
                    frqMatrix.setEntry(i, regionFeature.ordinal(), score);
                } catch (Exception _) {
                    logger.warn("Cannot find region feature for {}", regionScore.getValue2());
                }
            }
        }
        this.rFQMatrix = frqMatrix;
    }


    public void calculateLatentSpace(PreferenceDto preferenceDto) {
        RealMatrix mMatrix = rPOIMatrix.transpose().multiply(rFQMatrix);
        List<Integer> featureScores = preferenceDto.getFeatureDtos().stream().map(FeatureDto::getHasScore).toList();
        List<RegionFeature> regionFeatures =
                preferenceDto.getFeatureDtos().stream().map(FeatureDto::getRegionFeature).toList();
        RealVector xVector = new ArrayRealVector(
                Arrays.stream(RegionFeature.values()).map(existingFeature -> {
                    int idx = regionFeatures.indexOf(existingFeature);
                    if (idx != -1) {
                        return featureScores.get(idx);
                    } else {
                        return 0;
                    }
                }).toList().stream().mapToDouble(i -> i).toArray());

        SingularValueDecomposition svd = new SingularValueDecomposition(mMatrix);
        RealMatrix mInverse = svd.getSolver().getInverse();

        double[] singularValues = svd.getSingularValues();
        int rank = (int) Arrays.stream(singularValues).filter(s -> s > 1e-10).count();
        logger.info("Effective rank of M: {}", rank);

        RealMatrix xRowMatrix = new Array2DRowRealMatrix(xVector.toArray());
        xRowMatrix = xRowMatrix.transpose();  // Now 1×10

        RealMatrix yMatrix = xRowMatrix.multiply(mInverse);
        // latent vector that represents "ideal" POI
        RealVector yVector = yMatrix.getRowVector(0).mapMultiply(10000);


        double[] values = yVector.toArray();

        double[] uniqueValues = DoubleStream.of(values)
                .distinct()
                .toArray();

        System.out.print("yVector " + Arrays.toString(uniqueValues));
    }
}
