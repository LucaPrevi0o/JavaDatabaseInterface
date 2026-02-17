package com.lucaprevioo.jdbi;

import com.lucaprevioo.jdbi.transaction.Transaction;
import com.lucaprevioo.jdbi.transaction.TransactionBuilder;

import java.util.function.Consumer;

/// Abstract representation of a database that can manage multiple storage engines
/// using a common serializer type.
///
/// It provides methods to register storage engines, retrieve them based on model types,
/// and perform actions within a transaction context.
/// The use of a database allows for flexibility in managing different storage backends
/// while maintaining a consistent interface for model operations.
public class Database {

    /// Begin a transaction for the specified model type, executing the provided actions.
    ///
    /// @param <M>     The type of model for which to begin the transaction.
    /// @param <S>     The type of storage engine managing the model.
    /// @param engine  The storage engine instance for the specified model type.
    /// @param actions A consumer that defines the actions to be performed within the transaction.
    /// @return A Transaction object representing the transaction context.
    public <M extends Model, S extends StorageEngine<M>> Transaction<M, S> begin(S engine, Consumer<TransactionBuilder<M, S>> actions) {

        var builder = new TransactionBuilder<M, S>();
        actions.accept(builder);
        return new Transaction<>(engine, builder.build());
    }
}
