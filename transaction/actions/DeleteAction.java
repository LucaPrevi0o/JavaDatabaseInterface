package com.lucaprevioo.jdbi.transaction.actions;

import com.lucaprevioo.jdbi.transaction.Action;
import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

import java.util.function.Predicate;

public class DeleteAction<M extends Model, S extends StorageEngine<M>> implements Action<M, S> {

    private final Predicate<M> predicate;

    public DeleteAction(Predicate<M> predicate) { this.predicate = predicate; }

    @Override
    public <FK extends Model, FKS extends StorageEngine<FK>> void execute(S engine, FKS foreignKeyEngine) { engine.delete(predicate); }
}
