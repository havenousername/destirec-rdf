package org.destirec.destirec.rdf4j.userScores.externaldto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class HistoryExternalDto {
    private final String regionId;
    private final int score;
    private final Date fromDate;
    private final Date toDate;
}
