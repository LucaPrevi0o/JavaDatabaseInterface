package com.lucaprevioo.jdbi.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/// Utility class for parsing JSON strings into Java data structures without using external libraries.
///
/// This class provides methods to parse JSON strings into Maps and Lists, handling nested objects and arrays.
/// The parsing is done using regular expressions to identify key-value pairs, nested objects, and arrays within the
/// JSON string.
public class JsonUtilities {

    /// Capture nested objects from a JSON string.
    ///
    /// This method uses a regular expression to identify and extract key-object pairs from the JSON string.
    /// The regex looks for patterns where a key is followed by a colon and an object (enclosed in curly braces).
    /// The method then parses the object content using the fromJson method and stores the results in a list of maps,
    /// where each map contains a single key-object pair.
    /// @param jsonObject The JSON string to be processed, typically representing the content of a JSON object.
    /// @return A list of maps, where each map contains a single key-object pair representing a nested object field
    /// from the JSON string.
    private static List<Map<String, Object>> captureNestedObjects(String jsonObject) {

        var res = new ArrayList<Map<String, Object>>();
        var objectsRegex = Pattern.compile("\"([^\\[{\\]}\",]+)\":\\{([^}]+}*)}");
        var matcher = objectsRegex.matcher(jsonObject);
        while (matcher.find()) {

            var key = matcher.group(1);
            var value = matcher.group(2);
            var nestedMap = fromJson("{" + value + "}");
            res.add(Map.of(key, nestedMap));
        }
        return res;
    }

    /// Capture nested arrays from a JSON string.
    ///
    /// This method uses a regular expression to identify and extract key-array pairs from the JSON string.
    /// The regex looks for patterns where a key is followed by a colon and an array (enclosed in square brackets).
    /// The method then parses the array content using the fromJsonArray method and stores the results in a list of
    /// maps, where each map contains a single key-array pair.
    /// @param jsonObject The JSON string to be processed, typically representing the content of a JSON object.
    /// @return A list of maps, where each map contains a single key-array pair representing a nested array field from
    /// the JSON string.
    private static List<Map<String, List<Object>>> captureNestedArrays(String jsonObject) {

        var res = new ArrayList<Map<String, List<Object>>>();
        var arraysRegex = Pattern.compile("\"([^\\[{\\]}\",]+)\":\\[([^]]+]*)]");
        var matcher = arraysRegex.matcher(jsonObject);
        while (matcher.find()) {

            var key = matcher.group(1);
            var value = matcher.group(2);
            var nestedList = fromJsonArray("[" + value + "]");
            res.add(Map.of(key, nestedList));
        }
        return res;
    }

    /// Capture simple key-value pairs from a JSON string, excluding nested objects and arrays.
    ///
    /// This method uses a regular expression to identify and extract key-value pairs where the value is a primitive
    /// type (string, number, boolean, or null).
    /// The regex ensures that it does not match fields that are part of nested objects or arrays by using negative
    /// lookahead and lookbehind assertions.
    /// @param jsonArray The JSON string to be processed, typically representing the content of a JSON object or array.
    /// @return A list of maps, where each map contains a single key-value pair representing a simple field from the
    /// JSON string.
    private static List<Map<String, Object>> captureNestedFields(String jsonArray) {

        var res = new ArrayList<Map<String, Object>>();
        var objectsRegex = Pattern.compile("(?<![{\\[])\"([^\\[{\\]}\",]+)\":(\"([^\\[{\\]}\",]+)\"|([^\\[{\\]}\",]+))(?![]}])");
        var matcher = objectsRegex.matcher(jsonArray);
        while (matcher.find()) {

            var key = matcher.group(1);
            var value = matcher.group(2);
            res.add(Map.of(key, value));
        }
        return res;
    }

    private static List<Object> captureNestedArrayFields(String jsonArray) {

        var res = new ArrayList<>();
        var objectsRegex = Pattern.compile("\"[^\"]*\"|\\{[^}]+}|\\[[^]]+]|[^,\\[\\]]+");
        var matcher = objectsRegex.matcher(jsonArray);
        while (matcher.find()) {

            var value = matcher.group();
            if (value.startsWith("{") && value.endsWith("}")) res.add(fromJson(value));
            else if (value.startsWith("[") && value.endsWith("]")) res.add(fromJsonArray(value));
            else res.add(value);
        }
        return res;
    }

    /// Parse a JSON array string into a {@code List<Object>}.
    ///
    /// Each element in the JSON array will be represented as an Object in the resulting list.
    /// The method will handle basic JSON types such as strings, numbers, booleans, nulls, arrays, and nested objects.
    /// For arrays and nested objects, the elements will be represented as lists and maps respectively.
    /// @param json The JSON array string to be parsed.
    /// @return A {@code List<Object>} representing the parsed JSON array data.
    public static List<Object> fromJsonArray(String json) {

        var content = json.substring(1, json.length() - 1).trim();
        var nestedFields = captureNestedArrayFields(content);
        return new ArrayList<>(nestedFields);
    }

    /// Parse a JSON string into a {@code Map<String, Object>}.
    ///
    /// Every field in the JSON will be represented as a key-value pair in the resulting map, where the key is the field
    /// name and the value is the corresponding field value.
    /// The method will handle basic JSON types such as strings, numbers, booleans, nulls, arrays, and nested objects.
    /// For arrays and nested objects, the values will be represented as lists and maps respectively.
    /// @param json The JSON string to be parsed.
    /// @return A {@code Map<String, Object>} representing the parsed JSON data.
    public static Map<String, Object> fromJson(String json) {

        var content = json.substring(1, json.length() - 1).trim();
        var nestedFields = captureNestedFields(content);
        var nestedObjects = captureNestedObjects(content);
        var nestedArrays = captureNestedArrays(content);
        var res = new HashMap<String, Object>();
        for (var nested : nestedFields) res.putAll(nested);
        for (var nested : nestedObjects) res.putAll(nested);
        for (var nested : nestedArrays) res.putAll(nested);
        return res;
    }
}
