package com.lucaprevioo.jdbi.engine;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.Serializer;
import com.lucaprevioo.jdbi.StorageEngine;

import java.util.ArrayList;
import java.util.List;

/// An in-memory storage engine that keeps models in a list.
///
/// Using this storage engine, models are stored in memory and are not persisted to disk.
/// This is useful for testing or temporary storage scenarios.
///
/// @param <M> The type of model being stored.
public abstract class MemoryStorageEngine<M extends Model> extends StorageEngine<M> {

    List<M> storage = new ArrayList<>();

    /// Constructs a MemoryStorageEngine with the specified serializer.
    /// @param serializer The serializer used for serializing and deserializing models.
    public MemoryStorageEngine(Serializer serializer) { super(serializer); }

    @Override
    public void create(M model) { storage.add(model); }

    @Override
    public List<M> read() { return storage; }

    @Override
    public void update(M model, M updatedModel) {

        var index = storage.indexOf(model);
        if (index != -1) storage.set(index, updatedModel);
    }

    @Override
    public void delete(M model) { storage.remove(model); }
}
