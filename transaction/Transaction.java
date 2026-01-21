package com.lucaprevioo.jdbi.transaction;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

import java.util.List;

/// Represents a transaction context for performing actions on a specific storage engine.
/// @param <M> The type of model managed by the storage engine.
/// @param <S> The type of storage engine.
public class Transaction<M extends Model, S extends StorageEngine<M>> {

    private final S engine;
    private final List<Action<M>> actions;

    /// Constructs a Transaction with the specified storage engine and actions.
    /// @param engine  The storage engine instance for the transaction.
    /// @param actions A consumer that defines the actions to be performed within the transaction.
    public Transaction(S engine, List<Action<M>> actions) {

        this.engine = engine;
        this.actions = actions;
    }

    /// Commits the transaction by executing the defined actions on the storage engine.
    public void commit() {

        try { for (var action : actions) action.execute(engine); }
        catch (Exception e) { throw new RuntimeException("Transaction failed: " + e.getMessage(), e); }
    }

    /// Rolls back the transaction. (No-op in this implementation)
    public void rollback() { }
}