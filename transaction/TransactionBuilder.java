package com.lucaprevioo.jdbi.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;
import com.lucaprevioo.jdbi.transaction.actions.CreateAction;
import com.lucaprevioo.jdbi.transaction.actions.DeleteAction;
import com.lucaprevioo.jdbi.transaction.actions.UpdateAction;

/// A builder class for constructing a list of actions to be executed as part of a transaction. It provides methods to
/// create, update, and delete models in a fluent API style.
/// @param <M> The type of model being manipulated in the transaction.
/// @param <S> The type of storage engine that will execute the actions in the transaction
public class TransactionBuilder<M extends Model, S extends StorageEngine<M>> {

    private final List<Action<M, S>> actions = new ArrayList<>();

    /// Adds a create action to the transaction for the specified model.
    /// @param model The model instance to be created in the storage engine as part of the transaction.
    public void create(M model) { actions.add(new CreateAction<>(model)); }

    /// Adds create actions to the transaction for each model in the specified list.
    /// @param models A list of model instances to be created in the storage engine as part of the transaction.
    public void create(List<M> models) { for (var model : models) create(model); }

    /// Adds an update action to the transaction for the specified model, updating it to the provided updated model.
    /// @param model The existing model instance to be updated in the storage engine as part of the transaction.
    /// @param updatedModel The new model instance that will replace the existing model in the storage engine as part
    /// of the transaction.
    public void update(M model, M updatedModel) {
        actions.add(new UpdateAction<>(m -> m.equals(model), _ -> updatedModel));
    }

    /// Adds an update action to the transaction for models that match the specified predicate, using the provided
    /// updater function to determine how the matched models should be updated.
    /// @param predicate A predicate that defines the condition for selecting models to be updated in the storage engine
    /// as part of the transaction.
    /// @param updater A function that takes a model and returns an updated version of that model to be applied to the
    /// storage engine as part of the transaction.
    public void update(Predicate<M> predicate, Function<M, M> updater) {
        actions.add(new UpdateAction<>(predicate, updater));
    }

    /// Adds a delete action to the transaction for the specified model.
    /// @param model The model instance to be deleted from the storage engine as part of the transaction.
    public void delete(M model) { actions.add(new DeleteAction<>(m -> m.equals(model))); }

    /// Adds a delete action to the transaction for models that match the specified predicate.
    /// @param predicate A predicate that defines the condition for selecting models to be deleted from the storage
    /// engine as part of the transaction.
    public void delete(Predicate<M> predicate) { actions.add(new DeleteAction<>(predicate)); }

    /// Builds and returns the list of actions that have been added to the transaction builder. This list of actions
    /// can then be executed as part of a transaction to perform the specified create, update, and delete operations on
    /// the storage engine.
    /// @return A list of actions that have been added to the transaction builder, ready to be executed as part of a transaction.
    public List<Action<M, S>> build() { return actions; }
}
