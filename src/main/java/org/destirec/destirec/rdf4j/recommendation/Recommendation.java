package org.destirec.destirec.rdf4j.recommendation;

import org.destirec.destirec.rdf4j.region.RegionDto;
import org.destirec.destirec.rdf4j.user.UserDto;
import org.javatuples.Pair;

import java.util.List;

public record Recommendation(String type, List<Pair<UserDto, RegionDto>> entities) { }
