package jdbi;

import java.util.ArrayList;
import java.util.List;

public class MemoryStorageEngine<T> extends StorageEngine<T> {

    List<T> storage = new ArrayList<>();

    public MemoryStorageEngine(Serializer<T> serializer) { super(serializer); }

    @Override
    public List<T> read() { return storage; }

    @Override
    public void create(T model) { storage.add(model); }
}
