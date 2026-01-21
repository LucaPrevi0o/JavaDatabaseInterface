package com.lucaprevioo.jdbi.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.transaction.actions.CreateAction;
import com.lucaprevioo.jdbi.transaction.actions.DeleteAction;
import com.lucaprevioo.jdbi.transaction.actions.UpdateClass;

public class TransactionBuilder<M extends Model> {

    private final List<Action<M>> actions = new ArrayList<>();

    public void create(M model) { actions.add(new CreateAction<>(model)); }

    public void create(M[] models) { for (M model : models) create(model); }

    public void update(M model, M updatedModel) {
        actions.add(new UpdateClass<>(m -> m.equals(model), _ -> updatedModel));
    }

    public void update(Predicate<M> predicate, Function<M, M> updater) {
        actions.add(new UpdateClass<>(predicate, updater));
    }

    public void delete(M model) { actions.add(new DeleteAction<>(m -> m.equals(model))); }

    public void delete(Predicate<M> predicate) { actions.add(new DeleteAction<>(predicate)); }

    public List<Action<M>> build() { return actions; }
}
