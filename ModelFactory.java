package jdbi;

/// Factory class for creating model instances from serialized data.
///
/// Every Model type should have a corresponding ModelFactory implementation.
/// @param <T> The type of Model this factory creates.
public abstract class ModelFactory<T extends Model> {
    
    /// De-serialize the given data into a model instance.
    /// @param data The serialized data.
    /// @return The deserialized model instance.
    public abstract T deserialize(String data);
}
