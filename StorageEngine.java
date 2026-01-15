package jdbi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 * Storage engine for persisting and retrieving models.
 */
public class StorageEngine {

    String path;

    /**
     * Constructor.
     * @param path file path for storage
     */
    public StorageEngine(String path) { this.path = path; }

    /**
     * Write all models in the collection to storage.
     *
     * @param <T> type of model
     * @param <C> type of collection
     * @param collection collection of models to write
     * @throws IOException on write error
     */
    public <T extends Model, C extends Collection<T>> void write(C collection) throws IOException {

        var filePath = Path.of(path);
        try {

            var parent = filePath.getParent();
            if (parent != null) Files.createDirectories(parent);
            var lines = new ArrayList<String>();
            for (var model : collection.select()) lines.add(model.serialize());
            Files.write(filePath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) { throw new RuntimeException("Failed to write storage file", e); }
    }

    /**
     * Read stored lines and convert each line into a  using the provided
     * parser function.
     *
     * @param <T> type of model
     * @param <M> type of model factory
     * @param factory factory to create models from serialized data
     * @return collection of models read from storage
     * @throws IOException on read error
     */
    public <T extends Model, M extends ModelFactory<T>> Collection<T> read(M factory) throws IOException {

        var filePath = Path.of(path);
        if (!Files.exists(filePath)) return null;
        try {

            var lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            var collection = new Collection<T>("ReadCollection");
            for (var line : lines) collection.add(factory.deserialize(line));
            return collection;
        } catch (IOException e) { throw new RuntimeException("Failed to read storage file", e); }
    }
}
