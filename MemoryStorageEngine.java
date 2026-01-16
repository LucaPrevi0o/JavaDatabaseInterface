package jdbi;

import java.util.ArrayList;
import java.util.List;

public class MemoryStorageEngine<T> extends StorageEngine<T> {

    List<T> storage = new ArrayList<>();

    public MemoryStorageEngine(Serializer<T> serializer) { super(serializer); }

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
