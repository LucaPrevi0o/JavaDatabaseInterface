package com.lucaprevioo.jdbi.engine;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

import java.util.HashMap;
import java.util.Map;

/// Registry for storage engines, allowing dynamic retrieval based on model type.
public class EngineRegistry {

    private final Map<Class<?>, StorageEngine<?>> engines = new HashMap<>();

    /// Registers a storage engine for a specific model type.
    /// @param type The class of the model type.
    /// @param engine The storage engine instance to register for the model type.
    /// @param <M> The type of model.
    /// @param <S> The type of storage engine managing the model.
    public <M extends Model, S extends StorageEngine<M>> void register(Class<M> type, S engine) { engines.put(type, engine); }

    /// Retrieves the storage engine associated with the specified model type.
    /// @param type The class of the model type for which to retrieve the storage engine.
    /// @param <M> The type of model.
    /// @param <S> The type of storage engine managing the model.
    /// @return The storage engine instance associated with the specified model type.
    @SuppressWarnings("unchecked")
    public <M extends Model, S extends StorageEngine<M>> S get(Class<?> type) { return (S) engines.get(type); }
}