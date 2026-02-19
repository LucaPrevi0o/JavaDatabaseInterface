package com.lucaprevioo.jdbi.transaction.actions;

import com.lucaprevioo.jdbi.exception.ActionExecutionException;
import com.lucaprevioo.jdbi.exception.FailedValidationException;
import com.lucaprevioo.jdbi.transaction.Action;
import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

public class CreateAction<M extends Model, S extends StorageEngine<M>> implements Action<M, S> {

    private final M model;

    public CreateAction(M model) { this.model = model; }

    @Override
    public void execute(S engine) throws FailedValidationException {

        try {

            model.validate(engine);
            engine.create(model);
        } catch (FailedValidationException e) { throw new ActionExecutionException("Failed to create model: " + model + "\nCaused by: " + e.getMessage(), e); }
    }
}
