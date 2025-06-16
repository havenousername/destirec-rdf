package org.destirec.destirec.rdf4j.userScores;

import lombok.Getter;
import org.eclipse.rdf4j.model.IRI;

import java.util.List;

@Getter
public class UserInfluenceDto {
    private final IRI id;
    private final IRI region;
    private final IRI user;
    private final List<Double> scores;
    private final List<Double> confidences;


    public UserInfluenceDto(
            IRI id,
            IRI region,
            IRI user,
            List<Double> scores,
            List<Double> confidences
    ) {
        this.id = id;
        this.region = region;
        this.user = user;
        this.scores = scores;
        this.confidences = confidences;
    }
}
