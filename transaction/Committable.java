package com.lucaprevioo.jdbi.transaction;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;
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
    <FK extends Model, FKS extends StorageEngine<FK>> void execute(FKS foreignKeyEngine);

    /// Commits multiple transactions atomically. If any transaction fails, all transactions are rolled back.
    /// @param transactions An array of committable transactions to be executed together.
    static <FK extends Model, FKS extends StorageEngine<FK>>  void commit(List<? extends Committable> transactions, FKS foreignKeyEngine) {

        try {

            for (var t : transactions) t.snapshot();
            for (var t : transactions) t.execute(foreignKeyEngine);
        } catch (Exception e) {

            for (var t : transactions) try { t.rollback(); }
            catch (Exception re) { e.addSuppressed(new FailedRollbackException("Rollback failed", re)); }
            throw new FailedTransactionException("Rolled back due to transaction failure", e);
        }
    }

    /// Commits this transaction. If the commit fails, it rolls back to the previous state.
    default <FK extends Model, FKS extends StorageEngine<FK>>  void commit(FKS foreignKeyEngine) {

        try {

            snapshot();
            execute(foreignKeyEngine);
        } catch (Exception e) {

            try { rollback(); }
            catch (Exception re) { e.addSuppressed(new FailedRollbackException("Rollback failed", re)); }
            throw new FailedTransactionException("Rolled back due to transaction failure", e);
        }
    }
}
