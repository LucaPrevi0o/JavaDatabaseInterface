package com.lucaprevioo.jdbi.engine;

import com.lucaprevioo.jdbi.serializer.JsonSerializer;
import com.lucaprevioo.jdbi.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/// A storage engine that persists models in a JSON file using a JsonSerializer.
///
/// This engine reads and writes the entire collection of models as a single JSON array.
/// Each model is serialized/deserialized using the provided JsonSerializer.
/// @param <M> The type of model being stored, which must implement the Model interface.
public abstract class JsonStorageEngine<M extends Model> extends FileStorageEngine<M> {

    /// Constructs a JsonStorageEngine with the specified file path and JSON serializer.
    /// @param path The file path where models will be stored.
    /// @param serializer The JsonSerializer used for serializing and deserializing models.
    public JsonStorageEngine(String path, JsonSerializer serializer) { super(path, serializer); }

    @Override
    public synchronized void create(M model) {

        var models = read();
        models.add(model);
        writeModelsAsJson(models);
    }

    @Override
    public synchronized List<M> read() {

        var content = readFullFileContent();
        if (content.isEmpty()) return new ArrayList<>();

        var start = content.indexOf('[');
        var end = content.lastIndexOf(']');
        if (start == -1 || end == -1 || end <= start) return new ArrayList<>();

        var inner = content.substring(start + 1, end).trim();
        if (inner.isEmpty()) return new ArrayList<>();

        var out = new ArrayList<M>();
        var brace = 0;
        var sb = new StringBuilder();
        for (var i = 0; i < inner.length(); i++) {

            var c = inner.charAt(i);
            sb.append(c);
            if (c == '{') brace++;
            else if (c == '}') {

                brace--;
                if (brace == 0) {

                    var obj = sb.toString().trim();
                    if (!obj.isEmpty()) out.add(serializer.deserialize(obj));
                    sb.setLength(0);
                }
            }
        }
        return out;
    }

    @Override
    public synchronized void update(M model, M updatedModel) {

        var models = read();
        if (models.isEmpty()) return;
        var index = models.indexOf(model);
        if (index == -1) return;
        models.set(index, updatedModel);
        writeModelsAsJson(models);
    }

    @Override
    public synchronized void delete(M model) {

        var models = read();
        if (models.isEmpty()) return;
        boolean removed = models.remove(model);
        if (!removed) return;
        writeModelsAsJson(models);
    }

    /// Build a JSON array string from a list of models.
    /// @param models The list of models to serialize.
    /// @return A JSON array string representing the models.
    private String buildJson(List<M> models) {

        var sb = new StringBuilder();
        sb.append("[");
        for (var i = 0; i < models.size(); i++) {

            sb.append(serializer.serialize(models.get(i)));
            if (i < models.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /// Write the list of models to the storage file as a JSON array.
    /// @param models The list of models to write.
    private void writeModelsAsJson(List<M> models) {

        var json = buildJson(models);
        var lines = new ArrayList<String>();
        lines.add(json);
        try { atomicWriteSerializedLines(lines); }
        catch (Exception e) { getLogger().log(Level.SEVERE, "Failed to write models as json: " + getPath(), e); }
    }
}
