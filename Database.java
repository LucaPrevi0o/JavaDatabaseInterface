package com.lucaprevioo.jdbi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/// Abstract representation of a database that can manage multiple storage engines
/// using a common serializer type.
///
/// It provides methods to register storage engines, retrieve them based on model types,
/// and perform actions within a transaction context.
/// The use of a database allows for flexibility in managing different storage backends
/// while maintaining a consistent interface for model operations.
public abstract class Database {

    protected final List<StorageEngine> serializers = new ArrayList<>();

    /// Register a storage engine to be used by the database for a specific model type.
    /// @param <M> The type of model for which the storage engine is registered.
    /// @param <S> The type of the storage engine.
    /// @param storageEngine The storage engine to register.
    public <M extends Model, S extends StorageEngine<M>> void register(S storageEngine) {
        this.serializers.add(storageEngine);
    }

    /// Get a storage engine for the specified model type.
    /// @param <M> The type of model for which to get the storage engine.
    /// @return A StorageEngine for the specified model type.
    public <M extends Model> StorageEngine<M> getStorageEngine(Class<M> modelClass) {

        for (var engine : serializers)
            if (engine.getModelClass().equals(modelClass)) return engine;
        return null;
    }

    /// Begin a transaction for the specified model type, executing the provided actions.
    /// @param <M> The type of model for which to begin the transaction.
    /// @param modelClass The class of the model type.
    /// @param actions A consumer that defines the actions to be performed within the transaction.
    public <M extends Model> void beginTransaction(Class<M> modelClass, Consumer<StorageEngine<M>> actions) {

        var engine = getStorageEngine(modelClass);
        actions.accept(engine);
    }
}
