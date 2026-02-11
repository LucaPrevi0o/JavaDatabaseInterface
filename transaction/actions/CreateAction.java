package com.lucaprevioo.jdbi.transaction.actions;

import com.lucaprevioo.jdbi.transaction.Action;
import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

public class CreateAction<M extends Model> implements Action<M> {

    private final M model;

    public CreateAction(M model) { this.model = model; }

    @Override
    public void execute(StorageEngine<M> engine) {

        if (model.checkUniqueConstraints(engine)) throw new RuntimeException("Unique constraint violation for model: " + model);
        engine.create(model);
    }
}
