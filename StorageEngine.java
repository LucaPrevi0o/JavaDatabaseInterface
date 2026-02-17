package com.lucaprevioo.jdbi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/// Abstract storage engine for managing models.
///
/// Using a storage engine, models can be created, read, updated, and deleted (CRUD operations).
/// The storage engine relies on a serializer to handle the serialization and deserialization of models.
/// @param <M> The type of model managed by the storage engine.
public abstract class StorageEngine<M extends Model> {

    /// Create a single model in the storage.
    /// @param model The model to create in the storage.
    public abstract void create(M model);

    /// Create multiple models in the storage.
    /// @param models A list of models to create in the storage.
    public void create(List<M> models) { for (var model : models) create(model); }

    /// Read all models from the storage.
    /// @return A collection of all models read from the storage.
    public abstract List<M> read();

    /// Read models from the storage that match the given predicate.
    /// @param matcher A predicate to match models to be read.
    /// @return A list of models that match the given predicate.
    public List<M> read(Predicate<M> matcher) {

        var results = new ArrayList<M>();
        for (var item : read())
            if (matcher.test(item)) results.add(item);
        return results;
    }

    /// Update a model in the storage.
    /// @param model The existing model to be updated.
    /// @param updatedModel The updated model data.
    public abstract void update(M model, M updatedModel);

    /// Update models in the storage that match the given predicate.
    /// @param matcher A predicate to match models to be updated.
    /// @param updater A function to update the matched models.
    public void update(Predicate<M> matcher, Function<M, M> updater) {

        for (var t : read()) if (matcher.test(t))
            update(t, updater.apply(t));
    }

    /// Delete a model from the storage.
    /// @param model The model to be deleted.
    public abstract void delete(M model);

    /// Delete models from the storage that match the given predicate.
    /// @param matcher A predicate to match models to be deleted.
    public void delete(Predicate<M> matcher) {

        var snapshot = new ArrayList<>(read());
        for (var t : snapshot) if (matcher.test(t)) delete(t);
    }
}
