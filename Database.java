package jdbi;

import java.util.HashMap;
import java.util.Map;

/// Abstract representation of a database that can manage multiple storage engines
/// using a common serializer type.
/// @param <T> The type of serializer used by the storage engines in the database.
/// It provides the serialization and deserialization logic for the models.
public abstract class Database<T extends Serializer> {

    protected Map<Class<? extends Model>, T> serializers = new HashMap<>();

    /// Register a serializer to be used by storage engines in the database.
    /// @param <M> The type of model for which the serializer is registered.
    /// @param <S> The type of the serializer, which must extend T.
    /// @param serializer The serializer to register.
    public <M extends Model, S extends T> void registerSerializer(Class<M> modelClass, S serializer) { this.serializers.put(modelClass, serializer); }

    /// Get a storage engine for the specified model type.
    /// @param <M> The type of model for which to get the storage engine.
    /// @return A StorageEngine for the specified model type.
    public abstract <M extends Model> StorageEngine<M> getStorageEngine(Class<M> modelClass);
}
