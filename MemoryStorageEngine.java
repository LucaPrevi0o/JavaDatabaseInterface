package jdbi;

import java.util.ArrayList;
import java.util.List;

public class MemoryStorageEngine<T> extends StorageEngine<T> {

    List<T> storage = new ArrayList<>();

    /// Constructs a MemoryStorageEngine with the specified serializer.
    /// @param serializer The serializer used for serializing and deserializing models.
    public MemoryStorageEngine(Serializer serializer) { super(serializer); }

    @Override
    public void create(T model) { storage.add(model); }

    @Override
    public List<T> read() { return storage; }

    @Override
    public void update(T model, T updatedModel) {

        var index = storage.indexOf(model);
        if (index != -1) storage.set(index, updatedModel);
    }

    @Override
    public void delete(T model) { storage.remove(model); }
}
