package org.destirec.destirec.utils;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternDtoTransformations {
    private static final ValueFactory vf = SimpleValueFactory.getInstance();

    public static List<Pair<IRI, IRI>> toPairWithFeatureIds(String str, String type) {
        Pattern pattern = Pattern.compile("\\{"+ type +":\\s*(?<poiUrl>[^,]+),\\s*feature:\\s*(?<featureUrl>[^}]+)\\}");

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

    public static List<Pair<IRI, IRI>> toRegionFeaturePair(String str) {
        return toPairWithFeatureIds(str, "region");
    }

    public static List<Pair<IRI, IRI>> toPOIFeaturePair(String str) {
        return toPairWithFeatureIds(str, "poi");
    }
}
