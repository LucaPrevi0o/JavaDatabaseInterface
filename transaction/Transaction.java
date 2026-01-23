package com.lucaprevioo.jdbi.transaction;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;

import java.util.List;

/// Represents a transaction context for performing actions on a specific storage engine.
///
/// Using a transaction, multiple actions can be executed atomically, ensuring data integrity.
/// If any action fails, the transaction can be rolled back to restore the previous state.
/// @param <M> The type of model managed by the storage engine.
/// @param <S> The type of storage engine.
public class Transaction<M extends Model, S extends StorageEngine<M>> {

    private final S engine;
    private final List<Action<M>> actions;
    private List<M> snapshot = null;

    /// Constructs a Transaction with the specified storage engine and actions.
    /// @param engine  The storage engine instance for the transaction.
    /// @param actions A consumer that defines the actions to be performed within the transaction.
    public Transaction(S engine, List<Action<M>> actions) {

        this.engine = engine;
        this.actions = actions;
    }

    /// Commits the transaction by executing the defined actions on the storage engine.
    /// If any action fails, the transaction is rolled back to restore the previous state.
    public void commit() {

        try {

            snapshot = engine.read();
            for (var action : actions) action.execute(engine);
        } catch (Exception e) {

            try { rollback(); }
            catch (Exception re) { throw new RuntimeException("Rollback failed: " + re.getMessage(), re); }
        } finally { snapshot = null; }
    }

    /// Rolls back the transaction by restoring the storage engine to its previous state.
    public void rollback() {

        if (snapshot == null) throw new RuntimeException("No snapshot available for rollback.");
        engine.delete(_ -> true);
        engine.create(snapshot);
    }
}