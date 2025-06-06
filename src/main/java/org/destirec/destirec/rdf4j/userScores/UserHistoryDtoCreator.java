package org.destirec.destirec.rdf4j.userScores;

import lombok.Getter;
import org.destirec.destirec.rdf4j.interfaces.DtoCreator;
import org.destirec.destirec.utils.RdfDateParser;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Getter
public class UserHistoryDtoCreator implements DtoCreator<UserHistoryDto, UserHistoryConfig.Fields> {
    private final ValueFactory vf = SimpleValueFactory.getInstance();
    @Override
    public UserHistoryDto create(IRI id, Map<UserHistoryConfig.Fields, String> map) {
        return new UserHistoryDto(id,
                vf.createIRI(map.get(UserHistoryConfig.Fields.HAS_USER)),
                vf.createIRI(map.get(UserHistoryConfig.Fields.HAS_INFLUENCE)),
                vf.createIRI(map.get(UserHistoryConfig.Fields.HAS_ENTITY)),
                Double.parseDouble(map.getOrDefault(UserHistoryConfig.Fields.HAS_P_SCORE, "0")),
                RdfDateParser.parseRdfDate(map.getOrDefault(UserHistoryConfig.Fields.HAS_TIME_FROM, "2025-06-04")),
                RdfDateParser.parseRdfDate(map.getOrDefault(UserHistoryConfig.Fields.HAS_TIME_TO, "2025-06-04"))
        );
    }

    @Override
    public UserHistoryDto create(Map<UserHistoryConfig.Fields, String> map) {
        return create(null, map);
    }
}
