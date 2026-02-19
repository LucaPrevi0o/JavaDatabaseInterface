package com.lucaprevioo.jdbi.transaction;

import com.lucaprevioo.jdbi.exception.FailedTransactionException;

import java.util.List;

/// An interface representing a committable transaction, allowing for snapshotting, rolling back, and executing transactions.
public interface Committable {

    /// Take a snapshot of the current state before executing the transaction.
    void snapshot();

    /// Rollback the transaction, discarding all pending actions and restoring the previous state.
    void rollback();

    /// Execute the transaction, applying all pending actions.
    void execute();

    /// Commits multiple transactions atomically. If any transaction fails, all transactions are rolled back.
    /// @param transactions An array of committable transactions to be executed together.
    static void commit(List<? extends Committable> transactions) {

        try {

            for (var t : transactions) t.snapshot();
            for (var t : transactions) t.execute();
        } catch (Exception e) {

            System.err.println("Rolling back transaction due to commit failure: " + e.getMessage());
            for (var t : transactions) try { t.rollback(); }
            catch (Exception re) { throw new RuntimeException("Rollback failed ", re); }
        }
    }

    /// Commits this transaction. If the commit fails, it rolls back to the previous state.
    default void commit() {

        try {

            snapshot();
            execute();
        } catch (Exception e) {

            System.err.println("Rolling back transaction due to commit failure: " + e.getMessage());
            try { rollback(); }
            catch (Exception re) { throw new RuntimeException("Rollback failed ", re); }
        }
    }
}
