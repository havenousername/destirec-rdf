package org.destirec.destirec.rdf4j.userScores;

import org.destirec.destirec.rdf4j.interfaces.Rdf4jTemplate;
import org.destirec.destirec.rdf4j.poi.POIDto;
import org.destirec.destirec.rdf4j.region.RegionDao;
import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.region.RegionService;
import org.destirec.destirec.rdf4j.user.UserDao;
import org.destirec.destirec.rdf4j.user.UserDto;
import org.destirec.destirec.rdf4j.user.UserPreferenceService;
import org.destirec.destirec.rdf4j.userScores.externaldto.HistoryExternalDto;
import org.eclipse.rdf4j.model.IRI;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class UserActivityService {
    private final UserHistoryDao userHistoryDao;
    private final UserDao userDao;
    private final RegionDao regionDao;
    private final UserPreferenceService userPreferenceService;
    private final RegionService regionService;

    public UserActivityService(UserHistoryDao userHistoryDao, UserDao userDao, RegionDao regionDao, UserPreferenceService userPreferenceService, RegionService regionService, Rdf4jTemplate template) {
        this.userHistoryDao = userHistoryDao;
        this.userDao = userDao;
        this.regionDao = regionDao;
        this.userPreferenceService = userPreferenceService;
        this.regionService = regionService;
    }

    private double calculateConfidence(IRI regionId, Date from, Date to, double newScore) {
        long daysBetween = ChronoUnit.DAYS.between(from.toInstant(), to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        long daysFromNow = ChronoUnit.MONTHS.between(LocalDate.now(), to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        double averageScoreForRegion = regionDao.getRegionAvgScore(regionId);

        // sigmoid with center in seven days (0.5 confidence)
        var slope = 0.2;
        double durationConfidence = (1d / (1d + Math.exp(slope * (daysBetween - 7))));

        // 1\ -\ \frac{1}{1.5+\ e^{-0.3\left(x\ -\ 12\right)}} the closest
        // reviews are always the most significant
        double durationNewConfidence = (1d / (1d + Math.exp(-0.3 * (daysFromNow - 12))));

        double k = 1.0; // adjust: higher = sharper decay
        // quadratic difference
        double confidenceFromMean = 1/ ( 1 + k * Math.pow(newScore - averageScoreForRegion, 2));


        return (durationConfidence + durationNewConfidence + confidenceFromMean) / 3;
    }

    public UserHistoryDto saveVisit(HistoryExternalDto historyDto, String userId) {
        UserDto user =  userPreferenceService.getUser(userId);
        if(user == null) {
            throw new RuntimeException("User for string"  + userId + " not found");
        }

        Optional<RegionDto> region = regionService.getRegion(historyDto.getRegionId());
        Optional<POIDto> poiDto = regionService.getPOI(historyDto.getRegionId());

        IRI regionId;
        if (region.isPresent()) {
            regionId = region.get().getId();
        } else if (poiDto.isPresent()) {
            regionId = poiDto.get().getId();
        } else {
            throw new RuntimeException("RegionLike for string"  + historyDto.getRegionId() + " should be either region or " +
                    "poi. But not found");
        }

        IRI userInfluenceId;
        if (userHistoryDao.hasInfluence(regionId, user.id())) {
            Optional<UserInfluenceDto> userInfluenceDto = userHistoryDao.getInfluence(regionId, user.id());

            if (userInfluenceDto.isEmpty()) {
                throw new RuntimeException("Influence for region " + regionId + " and user " + user.id() + " not found");
            }
            // update user influence according to the formula
            // P_updated = min(1, \beta x P_user + (1 - \beta) \times P_score
            double newInfluenceScore = Math.min(5, UserInfluenceMigration.BETA * historyDto.getScore() + (1 - UserInfluenceMigration.BETA) * userInfluenceDto.get()
                    .getScores().getFirst());

            /**
             * update user confidence according to formula
             * C_updated = \beta * C_user + (1-\beta) C_user
             */
            double newConfidence = calculateConfidence(
                    regionId,
                    historyDto.getFromDate(),
                    historyDto.getToDate(),
                    historyDto.getScore()) * UserInfluenceMigration.BETA + (1 - UserInfluenceMigration.BETA) * userInfluenceDto.get().getConfidences().getLast();


            userInfluenceDto.get().getConfidences().add(newConfidence);
            userInfluenceDto.get().getScores().add(newInfluenceScore);
            userInfluenceId = userHistoryDao.updateInfluence(userInfluenceDto.get());
        } else {
            double newInfluenceScore = historyDto.getScore();
            double newConfidence = calculateConfidence(regionId,
                    historyDto.getFromDate(),
                    historyDto.getToDate(),
                    historyDto.getScore());

            List<Double> influencesArrayScores = new ArrayList<>();
            influencesArrayScores.add(newInfluenceScore);

            List<Double> influencesArrayConfidences = new ArrayList<>();
            influencesArrayConfidences.add(newConfidence);

            UserInfluenceDto influenceDto = new UserInfluenceDto(
                null,
                regionId,
                user.id(),
                influencesArrayScores,
                influencesArrayConfidences
            );
            userInfluenceId = userHistoryDao.createInfluence(influenceDto);
        }

        UserHistoryDto userHistoryDto = new UserHistoryDto(
            null,
            user.id(),
            userInfluenceId,
            regionId,
            historyDto.getScore(),
            historyDto.getFromDate(),
            historyDto.getToDate()
        );

        return userHistoryDao.save(userHistoryDto);
    }


    private void propagateInfluencesUp(UserInfluenceDto influenceDto) {
        Optional<RegionDto> regionDto = regionService.getRegion(influenceDto.getRegion());
        Optional<POIDto> poiDto = regionService.getPOI(influenceDto.getRegion());

        if (regionDto.isEmpty() && poiDto.isEmpty()) {
            throw new RuntimeException("RegionLike for string"  + influenceDto.getRegion() + " should be either region or " +
                    "poi. But not found");
        }

        // strategy for the region -> find its :level, propagate influences up and down
        if (regionDto.isPresent()) {
            var levelsUp = regionDto.get().getType().getTop();
//            var regionsOnTop = regionService.getFromREgion
        }
    }
}
