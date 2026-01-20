package com.lucaprevioo.jdbi;

/// Serializer interface for serializing and deserializing models.
///
/// Every serializer must implement methods to convert models to a storable format
/// and to reconstruct models from that format.
/// The use of generics allows the serializer to handle different types of models
/// while ensuring type safety.
///
/// <hr>
///
/// The serializer is designed to work with any model implementing the Model interface.
/// This allows for flexibility in handling different types of data models,
/// in order for the storage engines to utilize the same serialization logic.
public interface Serializer {

    /// Serialize the model into a storable line.
    /// @param model The model instance to serialize.
    /// @return The serialized representation of the model.
    <M extends Model> String serialize(M model);

    /// De-serialize the given data into a model instance.
    /// @param data The serialized data.
    /// @return The deserialized model instance.
    <M extends Model> M deserialize(String data);
}
