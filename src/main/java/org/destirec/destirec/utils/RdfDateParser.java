package org.destirec.destirec.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class RdfDateParser {
    public static Date parseRdfDate(String rdfDateStr) {
        // Strip datatype if present (e.g., ^^xsd:dateTime)
        if (rdfDateStr.contains("^^")) {
            rdfDateStr = rdfDateStr.split("\\^\\^")[0].replace("\"", "");
        }

        // Format for xsd:dateTime (e.g., 2025-06-04T15:30:00Z)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Adjust timezone if needed

        try {
            return sdf.parse(rdfDateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
