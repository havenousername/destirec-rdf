package org.destirec.destirec.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonValidator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static boolean isValidJsonObject(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return node.isObject();
        } catch (Exception e) {
            return false;
        }
    }
}
