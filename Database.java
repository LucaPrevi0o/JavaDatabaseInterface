package com.lucaprevioo.jdbi;

import com.lucaprevioo.jdbi.engine.EngineRegistry;
import com.lucaprevioo.jdbi.transaction.Transaction;
import com.lucaprevioo.jdbi.transaction.TransactionBuilder;

import java.util.function.Consumer;

/// The Database class serves as the main entry point for managing transactions on models using storage engines.
/// It provides a method to begin a transaction for a specified model type, allowing users to define the actions
/// to be performed within the transaction context.
public class Database {

    EngineRegistry registry = new EngineRegistry();

    /// Retrieves the engine registry associated with this database instance. The engine registry is responsible for
    /// managing the storage engines for different model types, allowing for dynamic retrieval and registration of
    /// storage engines as needed during transactions.
    /// @return The EngineRegistry instance associated with this database.
    public EngineRegistry getRegistry() { return registry; }

    /// Begin a transaction for the specified model type, executing the provided actions.
    /// @param <M> The type of model for which to begin the transaction.
    /// @param <S> The type of storage engine managing the model.
    /// @param engine The storage engine instance for the specified model type.
    /// @param actions A consumer that defines the actions to be performed within the transaction.
    /// @return A Transaction object representing the transaction context.
    public <M extends Model, S extends StorageEngine<M>> Transaction<M, S> begin(S engine, Consumer<TransactionBuilder<M, S>> actions) {

        registry.register(engine.getModelClass(), engine);
        var builder = new TransactionBuilder<M, S>();
        actions.accept(builder);
        return new Transaction<>(engine, builder.build(), registry);
    }
}
