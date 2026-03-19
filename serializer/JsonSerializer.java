package com.lucaprevioo.jdbi.serializer;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.Serializer;

import java.lang.reflect.Array;
import java.util.logging.Level;
import java.util.logging.Logger;

/// A serializer that converts models to and from JSON format.
///
/// This interface provides a default implementation for serializing models to JSON strings,
/// using reflection to access the model's fields.
/// It handles basic data types and performs simple escaping for JSON strings.
///
/// <hr>
///
/// The JsonSerializer is designed to work with any model implementing the Model interface.
/// This allows for flexibility in handling different types of data models,
/// in order for the storage engines to utilize JSON serialization logic.
public interface JsonSerializer<M extends Model> extends Serializer<M> {

    Logger logger = Logger.getLogger(JsonSerializer.class.getName());

    @Override
    default String serialize(M model) {

        var fields = model.getClass().getDeclaredFields();
        var jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        for (int i = 0; i < fields.length; i++) {

            fields[i].setAccessible(true);
            try {

                var name = fields[i].getName();
                var value = fields[i].get(model);
                appendField(jsonBuilder, name, value);
                if (i < fields.length - 1) jsonBuilder.append(",");
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "Failed to access field for JSON serialization: " + fields[i].getName(), e);
            }
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    /// Append a named field to the JSON string builder.
    /// @param jsonBuilder The StringBuilder to append to.
    /// @param name The name of the field.
    /// @param value The value of the field.
    private static void appendField(StringBuilder jsonBuilder, String name, Object value) {

        jsonBuilder.append("\"").append(name).append("\":");
        appendField(jsonBuilder, value);
    }

    /// Append a value to the JSON string builder.
    /// @param jsonBuilder The StringBuilder to append to.
    /// @param value The value to append.
    private static void appendField(StringBuilder jsonBuilder, Object value) {

        if (value == null) jsonBuilder.append("null");
        else if (value instanceof String || value instanceof Character) {

            var s = String.valueOf(value);
            jsonBuilder.append("\"").append(escapeJsonString(s)).append("\"");
        } else if (value instanceof Number || value instanceof Boolean) jsonBuilder.append(value);
        else if (value.getClass().isArray()) {

            jsonBuilder.append("[");
            var length = Array.getLength(value);
            for (var j = 0; j < length; j++) {

                var element = Array.get(value, j);
                appendField(jsonBuilder, element);
                if (j < length - 1) jsonBuilder.append(",");
            }
            jsonBuilder.append("]");
        } else jsonBuilder.append("\"").append(escapeJsonString(value.toString())).append("\"");
    }

    /// Escape special characters in a JSON string.
    /// @param s The input string to escape.
    /// @return The escaped JSON string.
    private static String escapeJsonString(String s) {

        if (s == null || s.isEmpty()) return s == null ? "" : s;
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
