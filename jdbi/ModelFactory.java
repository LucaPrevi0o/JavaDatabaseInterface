package jdbi;

public abstract class ModelFactory<T extends Model> {
    
    /**
     * storage engine uses this to create models from serialized data
     */
    public abstract T deserialize(String data);
}
