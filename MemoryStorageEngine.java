package jdbi;

import java.util.ArrayList;
import java.util.List;

public class MemoryStorageEngine<M extends Model> extends StorageEngine<M> {

    List<M> storage = new ArrayList<>();

    /// Constructs a MemoryStorageEngine with the specified serializer.
    /// @param serializer The serializer used for serializing and deserializing models.
    public MemoryStorageEngine(Serializer serializer) { super(serializer); }

    @Override
    public void create(M model) { storage.add(model); }

    @Override
    public List<M> read() { return storage; }

    @Override
    public void update(M model, M updatedModel) {

        var index = storage.indexOf(model);
        if (index != -1) storage.set(index, updatedModel);
    }

    @Override
    public void delete(M model) { storage.remove(model); }
}
