package jdbi;

/**
 * Abstract base class for all models in the JDBI framework.
 */
public abstract class Model {
    
    /**
     * Serialize the model into a storable line.
     * @return The serialized representation of the model.
     */
    public abstract String serialize();

    /**
     * De-serialize the given data into a model instance.
     * @param data The serialized data.
     * @return The deserialized model instance.
     */
    public abstract Model deserialize(String data);
}
