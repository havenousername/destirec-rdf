package org.destirec.destirec.utils;

import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SimpleDtoTransformations {
    private static ValueFactory vf = SimpleValueFactory.getInstance();
    public static String toStringIds(List<? extends Dto> dto) {
        return toStringIds(dto, ",");
    }

    public static String toStringIds(List<? extends Dto> dto, String delimiter) {
        return dto.stream()
                .map(d -> d.id() + delimiter)
                .collect(Collectors.joining());
    }

    public static String toIRIStringIds(List<? extends IRI> dto) {
        return dto.stream()
                .map(month -> month.stringValue() + ",")
                .collect(Collectors.joining());
    }

    public static List<String> toListString(String str, String delimiter) {
        return Arrays.stream(str.split(delimiter)).toList();
    }

    public static List<String> toListString(String str) {
        return toListString(str, ",");
    }


    public static List<Pair<IRI, IRI>> toFeaturePoiIds(String str) {
        Pattern pattern = Pattern.compile("\\{poi:\\s*(?<poiUrl>[^,]+),\\s*feature:\\s*(?<featureUrl>[^}]+)\\}");

        if (str == null || str.isBlank()) {
            return new ArrayList<>();
        }

        Matcher matcher = pattern.matcher(str);
        List<Pair<IRI, IRI>> result = new ArrayList<>();
        while (matcher.find()) {
            String poi = matcher.group("poiUrl").trim();
            String feature = matcher.group("featureUrl").trim();

            if (!poi.isEmpty() && !feature.isEmpty()) {
                result.add(new Pair<>(vf.createIRI(poi), vf.createIRI(feature)));
            }
        }

        return result;
    }
}
