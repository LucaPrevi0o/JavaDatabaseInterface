package com.lucaprevioo.jdbi.transaction.actions;

import com.lucaprevioo.jdbi.engine.EngineRegistry;
import com.lucaprevioo.jdbi.transaction.Action;
import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

/// An action that creates a new model in the storage engine. It validates the model before creation.
/// @param <M> The type of model to be created.
/// @param <S> The type of storage engine managing the model.
public class CreateAction<M extends Model, S extends StorageEngine<M>> implements Action<M, S> {

    private final M model;

    /// Constructs a CreateAction with the specified model to be created.
    /// @param model The model instance to be created in the storage engine.
    public CreateAction(M model) { this.model = model; }

    @Override
    public void execute(S engine, EngineRegistry registry) {

        model.validate(engine, registry);
        engine.create(model);
    }
}
