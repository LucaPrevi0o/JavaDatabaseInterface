package jdbi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class JsonStorageEngine<T> extends FileStorageEngine<T> {

    public JsonStorageEngine(String path, JsonSerializer<T> serializer) {
        super(path, serializer);
    }

    @Override
    public synchronized void create(T model) {
        var models = read();
        models.add(model);
        writeModelsAsJson(models);
    }

    @Override
    public synchronized List<T> read() {
        String content = readFullFileContent();
        if (content.isEmpty()) return new ArrayList<>();

        int start = content.indexOf('[');
        int end = content.lastIndexOf(']');
        if (start == -1 || end == -1 || end <= start) return new ArrayList<>();

        String inner = content.substring(start + 1, end).trim();
        if (inner.isEmpty()) return new ArrayList<>();

        List<T> out = new ArrayList<>();
        int brace = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            sb.append(c);
            if (c == '{') brace++;
            else if (c == '}') {
                brace--;
                if (brace == 0) {
                    String obj = sb.toString().trim();
                    if (!obj.isEmpty()) {
                        out.add(serializer.deserialize(obj));
                    }
                    sb.setLength(0);
                }
            }
        }
        return out;
    }

    @Override
    public synchronized void update(T model, T updatedModel) {
        var models = read();
        if (models.isEmpty()) return;
        var index = models.indexOf(model);
        if (index == -1) return;
        models.set(index, updatedModel);
        writeModelsAsJson(models);
    }

    @Override
    public synchronized void delete(T model) {
        var models = read();
        if (models.isEmpty()) return;
        boolean removed = models.remove(model);
        if (!removed) return;
        writeModelsAsJson(models);
    }

    // Build a compact JSON array string from models using the serializer
    private String buildJson(List<T> models) {
        var sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < models.size(); i++) {
            sb.append(serializer.serialize(models.get(i)));
            if (i < models.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // Use FileStorageEngine.atomicWriteSerializedLines to write the whole JSON array as a single line (atomic)
    private void writeModelsAsJson(List<T> models) {
        String json = buildJson(models);
        var lines = new ArrayList<String>();
        lines.add(json);
        try {
            atomicWriteSerializedLines(lines);
        } catch (Exception e) {
            // atomicWriteSerializedLines already logs errors; log here just in case
            getLogger().log(Level.SEVERE, "Failed to write models as json: " + getPath(), e);
        }
    }
}
