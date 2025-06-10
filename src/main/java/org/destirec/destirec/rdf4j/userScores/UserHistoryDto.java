package org.destirec.destirec.rdf4j.userScores;

import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.eclipse.rdf4j.model.IRI;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserHistoryDto implements Dto {
    private final IRI id;
    private final IRI userId;
    @Nullable
    private final Date fromTime;

    @Nullable
    private final Date toTime;

    private final IRI influenceId;
    private final double pScore;
    private final IRI regionIri;


    public UserHistoryDto(
            IRI id,
            IRI userId,
            IRI influenceId,
            IRI regionIri,
            double pScore,
            @Nullable
            Date fromTime,
            @Nullable
            Date toTime
    ) {
        this.id = id;
        this.userId = userId;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.influenceId = influenceId;
        this.pScore = pScore;
        this.regionIri = regionIri;
    }

    @Override
    public Map<UserHistoryConfig.Field, String> getMap() {
        Map<UserHistoryConfig.Field, String> map =
                new HashMap<>(Map.of(
                        UserHistoryConfig.Fields.HAS_USER, userId.stringValue(),
                        UserHistoryConfig.Fields.HAS_INFLUENCE, influenceId.stringValue(),
                        UserHistoryConfig.Fields.HAS_ENTITY, regionIri.stringValue(),
                        UserHistoryConfig.Fields.HAS_P_SCORE, pScore + ""
                ));
        if (fromTime != null) {
            map.put(UserHistoryConfig.Fields.HAS_TIME_FROM, fromTime.toString());
        }

        if (toTime != null) {
            map.put(UserHistoryConfig.Fields.HAS_TIME_FROM, toTime.toString());
        }
        return map;
    }

    @Override
    public IRI id() {
        return id;
    }
}
