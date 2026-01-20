package com.lucaprevioo.jdbi.engine;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.Serializer;
import com.lucaprevioo.jdbi.StorageEngine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/// An abstract storage engine that persists models in a file using line-based serialization.
///
/// Any implementation should define the serialization and deserialization format, by
/// providing a suitable Serializer instance to the constructor: the storge engine
/// itself is agnostic to the actual format used.
/// @param <M> The type of model being stored.
public abstract class FileStorageEngine<M extends Model> extends StorageEngine<M> {

    private static final Logger logger = Logger.getLogger(FileStorageEngine.class.getName());
    private final Path path;

    /// Constructs a FileStorageEngine with the specified file path and serializer.
    /// @param path The file path where models will be stored.
    /// @param serializer The serializer used for serializing and deserializing models.
    public FileStorageEngine(String path, Serializer serializer) {

        super(serializer);
        this.path = Path.of(path);
    }

    /// Gets the storage file path.
    /// @return The Path object representing the storage file path.
    public Path getPath() { return path; }

    /// Gets the logger for logging storage engine events.
    /// @return The Logger instance used for logging.
    protected Logger getLogger() { return logger; }

    /// Append a serialized line to the storage file.
    /// @param serialized The serialized line to append.
    protected void appendSerializedLine(String serialized) {

        var parent = path.getParent();
        if (parent != null) try { Files.createDirectories(parent); }
        catch (IOException e) { logger.log(Level.WARNING, "Failed to create parent directories for storage", e); }

        try (var w = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            w.write(serialized);
            w.newLine();
        } catch (IOException e) { logger.log(Level.SEVERE, "Failed to append model to storage file: " + path, e); }
    }

    /// Read all non-blank serialized lines from the storage file.
    /// @return A list of non-blank serialized lines, or `null` if file is missing or error occurs.
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

    /// Read the full content of the storage file as a single string.
    /// @return The full file content as a trimmed string, or `null` if file is missing or error occurs.
    protected String readFullFileContent() {

        if (!Files.exists(path)) return "";
        try { return Files.readString(path, StandardCharsets.UTF_8).trim(); }
        catch (IOException e) {

            logger.log(Level.SEVERE, "Failed to read full storage file content: " + path, e);
            return "";
        }
    }

    /// Atomically write the provided serialized lines to the storage file.
    /// @param lines The list of serialized lines to write.
    protected void atomicWriteSerializedLines(List<String> lines) {

        var parent = path.getParent();
        if (parent != null) try { Files.createDirectories(parent); }
        catch (IOException e) { logger.log(Level.WARNING, "Failed to create parent directories for storage", e); }

        Path tmp;
        try { tmp = Files.createTempFile(parent != null ? parent : Path.of("."), "tmp-storage-", ".tmp"); }
        catch (IOException e) {

            try (var w = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

                for (var l : lines) {

                    w.write(l);
                    w.newLine();
                }
            } catch (IOException ex) { logger.log(Level.SEVERE, "Failed to write models directly to storage file as fallback: " + path, ex); }
            return;
        }

        try (var w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {

            for (var l : lines) {

                w.write(l);
                w.newLine();
            }
        } catch (IOException e) {

            logger.log(Level.SEVERE, "Failed to write models to temporary file: " + tmp, e);
            try { Files.deleteIfExists(tmp); }
            catch (IOException ex) { logger.log(Level.FINE, "Failed to delete temporary file: " + tmp, ex); }
            return;
        }

        try { Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (IOException e) {

            logger.log(Level.SEVERE, "Failed to move temporary file to storage path: " + tmp + " -> " + path, e);
            try { Files.deleteIfExists(tmp); }
            catch (IOException ex) { logger.log(Level.FINE, "Failed to delete temporary file after failed move: " + tmp, ex); }
        }
    }

    @Override
    public synchronized void create(M model) { appendSerializedLine(serializer.serialize(model)); }

    @Override
    public synchronized List<M> read() {

        var lines = readAllSerializedLines();
        var out = new ArrayList<M>();
        for (var line : lines) out.add(serializer.deserialize(line));
        return out;
    }

    @Override
    public synchronized void update(M model, M updatedModel) {

        var models = read();
        if (models.isEmpty()) return;

        var index = models.indexOf(model);
        if (index == -1) return;
        models.set(index, updatedModel);

        var lines = new ArrayList<String>();
        for (var m : models) lines.add(serializer.serialize(m));
        atomicWriteSerializedLines(lines);
    }

    @Override
    public synchronized void delete(M model) {

        var models = read();
        if (models.isEmpty()) return;

        boolean removed = models.remove(model);
        if (!removed) return; // nothing to delete

        var lines = new ArrayList<String>();
        for (var m : models) lines.add(serializer.serialize(m));
        atomicWriteSerializedLines(lines);
    }
}
