package jdbi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/// Abstract storage engine for managing models.
///
/// Using a storage engine, models can be created, read, updated, and deleted (CRUD operations).
/// The storage engine relies on a serializer to handle the serialization and deserialization of models.
/// @param <T> The type of model managed by the storage engine.
public abstract class StorageEngine<T> {

    protected final Serializer serializer;

    /// Constructs a StorageEngine with the specified serializer.
    /// @param serializer The serializer to use for model serialization and deserialization.
    public StorageEngine(Serializer serializer) { this.serializer = serializer; }

    /// Get the serializer used by this storage engine.
    /// @return The serializer instance.
    public Serializer getSerializer() { return serializer; }

    /// Create a single model in the storage.
    /// @param model The model to create in the storage.
    public abstract void create(T model);

    /// Create multiple models in the storage.
    /// @param models A list of models to create in the storage.
    public void create(List<T> models) { for (var model : models) create(model); }

    /// Read all models from the storage.
    /// @return A collection of all models read from the storage.
    public abstract List<T> read();

    /// Read models from the storage that match the given predicate.
    /// @param matcher A predicate to match models to be read.
    /// @return A list of models that match the given predicate.
    public List<T> read(Predicate<T> matcher) {

        var results = new ArrayList<T>();
        for (var item : read())
            if (matcher.test(item)) results.add(item);
        return results;
    }

    /// Update a model in the storage.
    /// @param model The existing model to be updated.
    /// @param updatedModel The updated model data.
    public abstract void update(T model, T updatedModel);

    /// Update models in the storage that match the given predicate.
    /// @param matcher A predicate to match models to be updated.
    /// @param updater A function to update the matched models.
    public void update(Predicate<T> matcher, Function<T, T> updater) {

        for (var t : read()) if (matcher.test(t))
            update(t, updater.apply(t));
    }

    /// Delete a model from the storage.
    /// @param model The model to be deleted.
    public abstract void delete(T model);

    /// Delete models from the storage that match the given predicate.
    /// @param matcher A predicate to match models to be deleted.
    public void delete(Predicate<T> matcher) { for (var t : read()) if (matcher.test(t)) delete(t); }
}
