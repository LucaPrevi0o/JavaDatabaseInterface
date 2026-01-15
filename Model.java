package jdbi;

public abstract class Model {
    
    /**
     * storage engine uses this to convert a model into a storable line
     */
    public abstract String serialize();

    /**
     * factory class uses this to create models from serialized data
     */
    public abstract Model deserialize(String data);
}
