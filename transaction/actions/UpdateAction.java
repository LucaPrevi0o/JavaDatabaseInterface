package com.lucaprevioo.jdbi.transaction.actions;

import com.lucaprevioo.jdbi.engine.EngineRegistry;
import com.lucaprevioo.jdbi.transaction.Action;
import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

import java.util.function.Function;
import java.util.function.Predicate;

/// An action that updates models in the storage engine based on a specified predicate and an updater function.
/// @param <M> The type of model to be updated.
/// @param <S> The type of storage engine managing the model.
public class UpdateAction<M extends Model, S extends StorageEngine<M>> implements Action<M, S> {

    private final Predicate<M> predicate;
    private final Function<M, M> updater;

    /// Constructs an UpdateAction with the specified predicate and updater function.
    /// @param predicate A predicate that defines the condition for selecting models to be updated from the storage engine.
    /// @param updater A function that takes a model and returns an updated version of that model to be applied to the storage engine.
    public UpdateAction(Predicate<M> predicate, Function<M, M> updater) {

        this.predicate = predicate;
        this.updater = updater;
    }

    @Override
    public void execute(S engine, EngineRegistry registry) {

        engine.update(predicate, model -> {

            var updated = updater.apply(model);
            updated.validate(engine, registry);
            return updated;
        });
    }
}
