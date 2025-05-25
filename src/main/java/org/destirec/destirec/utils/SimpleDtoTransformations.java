package org.destirec.destirec.utils;

import org.destirec.destirec.rdf4j.interfaces.Dto;
import org.eclipse.rdf4j.model.IRI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleDtoTransformations {
    public static String toStringIds(List<? extends Dto> dto) {
        return dto.stream()
                .map(month -> month.id() + ",")
                .collect(Collectors.joining());
    }

    public static String toIRIStringIds(List<? extends IRI> dto) {
        return dto.stream()
                .map(month -> month.stringValue() + ",")
                .collect(Collectors.joining());
    }

    public static List<String> toListString(String str) {
        return Arrays.stream(str.split(",")).toList();
    }
}
