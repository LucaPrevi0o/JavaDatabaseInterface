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
}
