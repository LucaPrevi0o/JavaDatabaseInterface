package com.lucaprevioo.jdbi.transaction;

import com.lucaprevioo.jdbi.engine.EngineRegistry;
import com.lucaprevioo.jdbi.exception.FailedRollbackException;
import com.lucaprevioo.jdbi.exception.FailedTransactionException;

import java.util.List;

/// An interface representing a committable transaction, allowing for snapshotting, rolling back, and executing transactions.
public interface Committable {

    /// Take a snapshot of the current state before executing the transaction.
    void snapshot();

    /// Rollback the transaction, discarding all pending actions and restoring the previous state.
    void rollback();

    /// Execute the transaction, applying all pending actions.
    /// @param registry The EngineRegistry to be used for executing the transaction, providing access to the necessary
    /// storage engines.
    void execute();

    /// Commits multiple transactions atomically. If any transaction fails, all transactions are rolled back.
    /// @param transactions An array of committable transactions to be executed together.
    /// @param registry The EngineRegistry to be used for executing the transactions, providing access to the necessary
    /// storage engines.
    static void commit(List<? extends Committable> transactions) {

        try {

            for (var t : transactions) t.snapshot();
            for (var t : transactions) t.execute();
        } catch (Exception e) {

            for (var t : transactions) try { t.rollback(); }
            catch (Exception re) { e.addSuppressed(new FailedRollbackException("Rollback failed", re)); }
            throw new FailedTransactionException("Rolled back due to transaction failure", e);
        }
    }

    /// Commits this transaction. If the commit fails, it rolls back to the previous state.
    /// @param registry The EngineRegistry to be used for executing the transaction, providing access to the necessary
    /// storage engines.
    default void commit() {

        try {

            snapshot();
            execute();
        } catch (Exception e) {

            try { rollback(); }
            catch (Exception re) { e.addSuppressed(new FailedRollbackException("Rollback failed", re)); }
            throw new FailedTransactionException("Rolled back due to transaction failure", e);
        }
    }
}
