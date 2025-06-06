package org.destirec.destirec.rdf4j.userScores;

import lombok.Getter;
import org.eclipse.rdf4j.model.IRI;

@Getter
public class UserInfluenceDto {
    private final IRI id;
    private final IRI region;
    private final IRI user;
    private final double score;
    private final double confidence;

    public UserInfluenceDto(IRI id, IRI region, IRI user, double score, double confidence) {
        this.id = id;
        this.region = region;
        this.user = user;
        this.score = score;
        this.confidence = confidence;
    }
}
