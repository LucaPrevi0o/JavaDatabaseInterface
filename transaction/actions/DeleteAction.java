package com.lucaprevioo.jdbi.transaction.actions;

import com.lucaprevioo.jdbi.engine.EngineRegistry;
import com.lucaprevioo.jdbi.transaction.Action;
import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

import java.util.function.Predicate;

/// An action that deletes models from the storage engine based on a specified predicate.
/// @param <M> The type of model to be deleted.
/// @param <S> The type of storage engine managing the model.
public class DeleteAction<M extends Model, S extends StorageEngine<M>> implements Action<M, S> {

    private final Predicate<M> predicate;

    /// Constructs a DeleteAction with the specified predicate to determine which models to delete.
    /// @param predicate A predicate that defines the condition for selecting models to be deleted from the
    public DeleteAction(Predicate<M> predicate) { this.predicate = predicate; }

    @Override
    public void execute(S engine, EngineRegistry registry) { engine.delete(predicate); }
}
