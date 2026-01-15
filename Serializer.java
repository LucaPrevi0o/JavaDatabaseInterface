package jdbi;

/// Serializer interface for serializing and deserializing models.
/// @param <T> The type of model to be serialized/deserialized.
public interface Serializer<T> {

    /// Serialize the model into a storable line.
    /// @param model The model instance to serialize.
    /// @return The serialized representation of the model.
    String serialize(T model);
    
    /// De-serialize the given data into a model instance.
    /// @param data The serialized data.
    /// @return The deserialized model instance.
    T deserialize(String data);
}
