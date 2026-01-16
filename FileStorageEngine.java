package jdbi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FileStorageEngine<T> extends StorageEngine<T> {

    private static final Logger logger = Logger.getLogger(FileStorageEngine.class.getName());
    private final Path path;

    public FileStorageEngine(String path, Serializer<T> serializer) {

        super(serializer);
        this.path = Path.of(path);
    }

    public Path getPath() { return path; }
    protected Logger getLogger() { return logger; }

    // Protected helpers for subclasses to use when implementing format-specific behavior.
    // Append a single serialized line to the storage file (creates parent dirs if needed).
    protected void appendSerializedLine(String serialized) {
        var parent = path.getParent();
        if (parent != null) try { Files.createDirectories(parent); }
        catch (IOException e) { logger.log(Level.WARNING, "Failed to create parent directories for storage", e); }

        try (var w = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(serialized);
            w.newLine();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to append model to storage file: " + path, e);
        }
    }

    // Read all non-blank serialized lines from the storage file.
    protected List<String> readAllSerializedLines() {
        if (!Files.exists(path)) return new ArrayList<>();
        try {
            var lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            var out = new ArrayList<String>();
            for (var line : lines) if (!line.isBlank()) out.add(line);
            return out;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read storage file: " + path, e);
            return new ArrayList<>();
        }
    }

    // Read entire file content as a single trimmed string. Returns empty string if file missing or error.
    protected String readFullFileContent() {
        if (!Files.exists(path)) return "";
        try {
            return Files.readString(path, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read full storage file content: " + path, e);
            return "";
        }
    }

    // Helper: write the entire list of serialized lines atomically using a temp file in the same directory.
    protected void atomicWriteSerializedLines(List<String> lines) {
        var parent = path.getParent();
        if (parent != null) {
            try { Files.createDirectories(parent); } catch (IOException e) { logger.log(Level.WARNING, "Failed to create parent directories for storage", e); }
        }

        Path tmp;
        try {
            tmp = Files.createTempFile(parent != null ? parent : Path.of("."), "tmp-storage-", ".tmp");
        } catch (IOException e) {
            // Fallback: write directly to the target path
            try (var w = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (var l : lines) {
                    w.write(l);
                    w.newLine();
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Failed to write models directly to storage file as fallback: " + path, ex);
            }
            return;
        }

        try (var w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            for (var l : lines) {
                w.write(l);
                w.newLine();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to write models to temporary file: " + tmp, e);
            try { Files.deleteIfExists(tmp); } catch (IOException ex) {
                logger.log(Level.FINE, "Failed to delete temporary file: " + tmp, ex);
            }
            return;
        }

        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to move temporary file to storage path: " + tmp + " -> " + path, e);
            try { Files.deleteIfExists(tmp); } catch (IOException ex) {
                logger.log(Level.FINE, "Failed to delete temporary file after failed move: " + tmp, ex);
            }
        }
    }

    @Override
    public synchronized void create(T model) {
        // Default line-based behavior: append the serialized model as a line.
        appendSerializedLine(serializer.serialize(model));
    }

    @Override
    public synchronized List<T> read() {
        var lines = readAllSerializedLines();
        var out = new ArrayList<T>();
        for (var line : lines) out.add(serializer.deserialize(line));
        return out;
    }

    @Override
    public synchronized void update(T model, T updatedModel) {
        var models = read();
        if (models.isEmpty()) return;

        var index = models.indexOf(model);
        if (index == -1) return; // nothing to update
        models.set(index, updatedModel);

        var lines = new ArrayList<String>();
        for (var m : models) lines.add(serializer.serialize(m));
        atomicWriteSerializedLines(lines);
    }

    @Override
    public synchronized void delete(T model) {
        var models = read();
        if (models.isEmpty()) return;

        boolean removed = models.remove(model);
        if (!removed) return; // nothing to delete

        var lines = new ArrayList<String>();
        for (var m : models) lines.add(serializer.serialize(m));
        atomicWriteSerializedLines(lines);
    }
}
