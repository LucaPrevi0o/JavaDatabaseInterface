package jdbi;

import java.util.logging.Level;
import java.util.logging.Logger;

public interface JsonSerializer<T> extends Serializer<T>{

    Logger logger = Logger.getLogger(JsonSerializer.class.getName());

    @Override
    default String serialize(T model) {

        var fields = model.getClass().getDeclaredFields();
        var jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        for (int i = 0; i < fields.length; i++) {

            fields[i].setAccessible(true);
            try {

                var name = fields[i].getName();
                var value = fields[i].get(model);
                jsonBuilder.append("\"").append(name).append("\":");

                if (value == null) {
                    jsonBuilder.append("null");
                } else if (value instanceof String || value instanceof Character) {
                    var s = String.valueOf(value);
                    jsonBuilder.append("\"").append(escapeJsonString(s)).append("\"");
                } else if (value instanceof Number || value instanceof Boolean) {
                    jsonBuilder.append(value.toString());
                } else {
                    // Fallback: call toString() and quote it (best-effort for complex types)
                    jsonBuilder.append("\"").append(escapeJsonString(value.toString())).append("\"");
                }
                if (i < fields.length - 1) jsonBuilder.append(",");
            } catch (IllegalAccessException e) { logger.log(Level.SEVERE, "Failed to access field for JSON serialization: " + fields[i].getName(), e); }
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    static String escapeJsonString(String s) {
        if (s == null || s.isEmpty()) return s == null ? "" : s;
        // simple escaping for common JSON control characters
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
