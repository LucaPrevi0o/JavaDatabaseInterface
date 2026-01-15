import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Collection<T extends Model> extends Model {
    
    String name;
    List<T> items = new ArrayList<>();

    public Collection(String name) { this.name = name; }

    public String getName() { return name; }

    public void add(T model) { items.add(model); }

    public void add(T[] models) { for (var model : models) add(model); }

    public List<T> select(Predicate<T> condition) {
        
        var results = new ArrayList<T>();
        for (var record : items)
            if (condition.test(record)) results.add(record);
        return results;
    }

    public List<T> select() { return items; }

    public void remove(Predicate<T> condition) { items.removeIf(condition); }

    @Override
    public String serialize() { return "Collection{" + "name='" + name + ", items=" + items + '}'; }

    @Override
    public Collection<T> deserialize(String data) {

        var parts = data.split(",", 2);
        var name = parts[0];
        var collection = new Collection<T>(name);
        // Deserialization of items is not implemented here
        return collection;
    }
}
