package org.destirec.destirec.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class URIHandling {
    public static String toSafeIRIForm(String input) {
            if (input == null || input.trim().isEmpty()) {
                throw new IllegalArgumentException("Input string cannot be null or empty");
            }

            try {
                // Basic cleaning
                String cleaned = input
                        .trim()
                        .replaceAll("[\\s\\p{Punct}&&[^-_]]+", "")
                        .replaceAll("[àáâãäåāăąǎǟǡǻȁȃạảấầẩẫậắằẳẵặ]", "a")
                        .replaceAll("[èéêëēĕėęěȅȇẹẻẽếềểễệ]", "e")
                        .replaceAll("[ìíîïĩīĭįǐȉȋḭḯỉịớờởỡợ]", "i")
                        .replaceAll("[òóôõöōŏőơǒǫǭȍȏọỏốồổỗộớờởỡợ]", "o")
                        .replaceAll("[ùúûüũūŭůűųưǔǖǘǚǜȕȗụủứừửữự]", "u")
                        .replaceAll("[^\\p{ASCII}]", "");

                new URI(cleaned);

                return cleaned;
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Cannot create valid IRI from input: " + input, e);
            }
    }

    public static String getOrigin(String uriStr) {
        try {
            URI uri = new URI(uriStr);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();
            return port == -1 ? String.format("%s://%s", scheme, host)
                    : String.format("%s://%s:%d", scheme, host, port);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URI: " + uriStr, e);
        }
    }
}
