package com.lucaprevioo.jdbi;

import java.util.function.Consumer;

/// Abstract representation of a database that can manage multiple storage engines
/// using a common serializer type.
///
/// It provides methods to register storage engines, retrieve them based on model types,
/// and perform actions within a transaction context.
/// The use of a database allows for flexibility in managing different storage backends
/// while maintaining a consistent interface for model operations.
public abstract class Database {

    /// Represents a transaction context for performing actions on a specific storage engine.
    /// @param <M> The type of model managed by the storage engine.
    /// @param <S> The type of storage engine.
    public static class Transaction<M extends Model, S extends StorageEngine<M>> {

        private final S engine;
        private final Consumer<S> actions;

        /// Constructs a Transaction with the specified storage engine and actions.
        /// @param engine  The storage engine instance for the transaction.
        /// @param actions A consumer that defines the actions to be performed within the transaction.
        public Transaction(S engine, Consumer<S> actions) {

            this.engine = engine;
            this.actions = actions;
        }

        /// Commits the transaction by executing the defined actions on the storage engine.
        public void commit() { actions.accept(engine); }

        /// Rolls back the transaction. (No-op in this implementation)
        public void rollback() { }
    }

    /// Begin a transaction for the specified model type, executing the provided actions.
    ///
    /// @param <M>     The type of model for which to begin the transaction.
    /// @param <S>     The type of storage engine managing the model.
    /// @param engine  The storage engine instance for the specified model type.
    /// @param actions A consumer that defines the actions to be performed within the transaction.
    /// @return A Transaction object representing the transaction context.
    public <M extends Model, S extends StorageEngine<M>> Transaction<M, S> beginTransaction(S engine, Consumer<S> actions) {
        return new Transaction<>(engine, actions);
    }
}
