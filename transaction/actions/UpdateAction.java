package com.lucaprevioo.jdbi.transaction.actions;

import com.lucaprevioo.jdbi.transaction.Action;
import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

import java.util.function.Function;
import java.util.function.Predicate;

public class UpdateAction<M extends Model> implements Action<M> {

    private final Predicate<M> predicate;
    private final Function<M, M> updater;

    public UpdateAction(Predicate<M> predicate, Function<M, M> updater) {

        this.predicate = predicate;
        this.updater = updater;
    }

    @Override
    public void execute(StorageEngine<M> engine) {

        var value = updater.apply(null);
        if (value.checkUniqueConstraints(engine)) throw new RuntimeException("Unique constraint violation for updated model: " + value);
        if (value.checkNotNullConstraints(engine)) throw new RuntimeException("Not-null constraint violation for updated model: " + value);
        engine.update(predicate, updater);
    }
}
