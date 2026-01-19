package com.lucaprevioo.jdbi;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/// Abstract representation of a database that can manage multiple storage engines
/// using a common serializer type.
/// @param <T> The type of serializer used by the storage engines in the database.
/// It provides the serialization and deserialization logic for the models.
public abstract class Database {

    protected final Map<Class<? extends Model>, StorageEngine> serializers = new HashMap<>();

    /// Register a storage engine to be used by the database for a specific model type.
    /// @param <M> The type of model for which the storage engine is registered.
    /// @param <S> The type of the storage engine, which must extend T.
    /// @param storageEngine The storage engine to register.
    public <M extends Model, S extends StorageEngine<M>> void register(Class<M> modelClass, S storageEngine) {
        this.serializers.put(modelClass, storageEngine);
    }

    /// Get a storage engine for the specified model type.
    /// @param <M> The type of model for which to get the storage engine.
    /// @return A StorageEngine for the specified model type.
    public <M extends Model> StorageEngine<M> getStorageEngine(Class<M> modelClass) {
        return serializers.get(modelClass);
    }

    public <M extends Model> void beginTransaction(Class<M> modelClass, Consumer<StorageEngine<M>> actions) {

        var engine = getStorageEngine(modelClass);
        actions.accept(engine);
    }
}
