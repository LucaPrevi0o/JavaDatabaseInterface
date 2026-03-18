package com.lucaprevioo.jdbi.transaction;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;
import com.lucaprevioo.jdbi.engine.EngineRegistry;

import java.util.List;

/// Represents a transaction context for performing actions on a specific storage engine.
///
/// Using a transaction, multiple actions can be executed atomically, ensuring data integrity.
/// If any action fails, the transaction can be rolled back to restore the previous state.
/// @param <M> The type of model managed by the storage engine.
/// @param <S> The type of storage engine.
public class Transaction<M extends Model, S extends StorageEngine<M>> implements Committable {

    private final S engine;
    private final List<Action<M, S>> actions;
    private List<M> snapshot = null;
    private final EngineRegistry registry;

    /// Constructs a Transaction with the specified storage engine and actions.
    /// @param engine  The storage engine instance for the transaction.
    /// @param actions A consumer that defines the actions to be performed within the transaction.
    public Transaction(S engine, List<Action<M, S>> actions, EngineRegistry registry) {

        this.engine = engine;
        this.actions = actions;
        this.registry = registry;
    }

    @Override
    public void snapshot() { snapshot = engine.read();}

    @Override
    public void execute() { for (var action : actions) action.execute(engine, registry); }

    @Override
    public void rollback() {

        if (snapshot == null) throw new RuntimeException("No snapshot available for rollback.");
        engine.delete(_ -> true);
        engine.create(snapshot);
    }
}