package com.lucaprevioo.jdbi.transaction.actions;

import com.lucaprevioo.jdbi.transaction.Action;
import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

public class CreateAction<M extends Model, S extends StorageEngine<M>> implements Action<M, S> {

    private final M model;

    public CreateAction(M model) { this.model = model; }

    @Override
    public <FK extends Model, FKS extends StorageEngine<FK>> void execute(S engine, FKS foreignKeyEngine) {

        model.validate(engine, foreignKeyEngine);
        engine.create(model);
    }
}
