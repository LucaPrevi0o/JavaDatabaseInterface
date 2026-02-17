package com.lucaprevioo.jdbi.transaction;

/// An interface representing a committable transaction, allowing for snapshotting, rolling back, and executing transactions.
public interface Committable {

    /// Take a snapshot of the current state before executing the transaction.
    void snapshot();

    /// Rollback the transaction, discarding all pending actions and restoring the previous state.
    void rollback();

    /// Execute the transaction, applying all pending actions.
    void execute();
}
