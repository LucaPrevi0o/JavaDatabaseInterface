package com.lucaprevioo.jdbi.transaction.actions;

import com.lucaprevioo.jdbi.transaction.Action;
import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

import java.util.function.Function;
import java.util.function.Predicate;

public class UpdateClass<M extends Model> implements Action<M> {

    private final Predicate<M> predicate;
    private final Function<M, M> updater;

    public UpdateClass(Predicate<M> predicate, Function<M, M> updater) {

        this.predicate = predicate;
        this.updater = updater;
    }

    @Override
    public void execute(StorageEngine<M> engine) { engine.update(predicate, updater); }
}
