package com.lucaprevioo.jdbi.transaction.actions;

import com.lucaprevioo.jdbi.engine.EngineRegistry;
import com.lucaprevioo.jdbi.transaction.Action;
import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

import java.util.function.Function;
import java.util.function.Predicate;

public class UpdateAction<M extends Model, S extends StorageEngine<M>> implements Action<M, S> {

    private final Predicate<M> predicate;
    private final Function<M, M> updater;

    public UpdateAction(Predicate<M> predicate, Function<M, M> updater) {

        this.predicate = predicate;
        this.updater = updater;
    }

    @Override
    public void execute(S engine, EngineRegistry registry) { engine.update(predicate, updater); }
}
