package jdbi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/// Collection of Model instances.
///
/// This class provides methods to add, select, and remove models based on conditions.
/// It also supports serialization and deserialization of the collection.
/// @param <T> type of Model contained in the collection
public class Collection<T extends Model> extends Model {
    
    String name;
    List<T> items = new ArrayList<>();

    /// Constructs a Collection with the specified name.
    /// @param name the name of the collection
    public Collection(String name) { this.name = name; }

    /// Returns the name of the collection.
    /// @return the name of the collection
    public String getName() { return name; }

    /// Adds a model to the collection.
    /// @param model the model to add
    public void add(T model) { items.add(model); }

    /// Adds multiple models to the collection.
    /// @param models array of models to add
    public void add(T[] models) { for (var model : models) add(model); }

    /// Selects models from the collection that satisfy the given condition.
    /// @param condition the predicate condition to filter models
    /// @return list of models that satisfy the condition
    public List<T> select(Predicate<T> condition) {
        
        var results = new ArrayList<T>();
        for (var record : items)
            if (condition.test(record)) results.add(record);
        return results;
    }

    /// Selects all models from the collection.
    /// @return list of all models in the collection
    public List<T> select() { return items; }

    /// Removes models from the collection that satisfy the given condition.
    /// @param condition the predicate condition to filter models for removal
    public void remove(Predicate<T> condition) { items.removeIf(condition); }

    @Override
    public String serialize() { return "Collection{" + "name='" + name + ", items=" + items + '}'; }

    @Override
    public Collection<T> deserialize(String data) {

        var parts = data.split(",", 2);
        var name = parts[0];
        return new Collection<>(name);
    }
}
