package com.lucaprevioo.jdbi.transaction;

import com.lucaprevioo.jdbi.Model;
import com.lucaprevioo.jdbi.StorageEngine;
import com.lucaprevioo.jdbi.engine.EngineRegistry;

/// An action represents a single operation to be performed on a storage engine within a transaction.
/// It encapsulates the logic for executing the operation and can be used to define various types of actions such as
/// create, update, delete, etc.
/// @param <M> The type of model that the action operates on, which must extend the Model interface.
/// @param <S> The type of storage engine that manages the model, which must extend the StorageEngine interface.
@FunctionalInterface
public interface Action<M extends Model, S extends StorageEngine<M>> {

    /// Executes the action using the provided storage engine and engine registry. The implementation of this method
    /// will contain the specific logic for performing the desired operation on the storage engine.
    /// @param engine The storage engine instance on which the action will be performed.
    /// @param registry The engine registry that may be used to access other storage engines or resources needed for the action.
    void execute(S engine, EngineRegistry registry);
}
