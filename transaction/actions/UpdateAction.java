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

        // Validate all updated models on a snapshot before applying updates to avoid partial updates
        var snapshot = engine.read();
        for (var item : snapshot) if (predicate.test(item)) {

            var updated = updater.apply(item);
            if (!updated.validate(engine)) throw new RuntimeException("Validation failed for updated model: " + updated);
        }

        // All validations passed, perform the update
        engine.update(predicate, updater);
    }
}
