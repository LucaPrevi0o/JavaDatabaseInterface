package jdbi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileStorageEngine<T> extends StorageEngine<T> {

    private final Path path;

    public FileStorageEngine(String path, Serializer<T> serializer) {

        super(serializer);
        this.path = Path.of(path);
    }

    @Override
    public synchronized void create(T model) {

        var parent = path.getParent();
        if (parent != null) try { Files.createDirectories(parent); }
        catch (IOException ignored) {}

        Path tmp = null;
        try {
            tmp = Files.createTempFile(parent != null ? parent : Path.of("."), "tmp-storage-", ".tmp");
        } catch (IOException ignored) {}
        try (var w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {

            w.write(serializer.serialize(model));
            w.newLine();
        } catch (IOException ignored) {}

        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ignored) {}
    }

    @Override
    public synchronized List<T> read() {

        if (!Files.exists(path)) return null;
        try {

            var lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            var out = new ArrayList<T>();
            for (var line : lines) out.add(serializer.deserialize(line));
            return out;
        } catch (IOException e) { return null; }
    }

    @Override
    public synchronized void update(T model, T updatedModel) {

        var models = read();
        if (models == null) return;

        var index = models.indexOf(model);
        if (index != -1) models.set(index, updatedModel);

        Path tmp = null;
        try {
            tmp = Files.createTempFile(path.getParent() != null ? path.getParent() : Path.of("."), "tmp-storage-", ".tmp");
        } catch (IOException ignored) {}
        try (var w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {

            for (var m : models) {
                w.write(serializer.serialize(m));
                w.newLine();
            }
        } catch (IOException ignored) {}

        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ignored) {}
    }

    @Override
    public synchronized void delete(T model) {

        var models = read();
        if (models == null) return;

        models.remove(model);

        Path tmp = null;
        try {
            tmp = Files.createTempFile(path.getParent() != null ? path.getParent() : Path.of("."), "tmp-storage-", ".tmp");
        } catch (IOException ignored) {}
        try (var w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {

            for (var m : models) {
                w.write(serializer.serialize(m));
                w.newLine();
            }
        } catch (IOException ignored) {}

        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ignored) {}
    }
}
