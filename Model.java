package com.lucaprevioo.jdbi;

/// Marker interface for data models.
///
/// Models are plain Java objects that represent data entities in the storage engine.
/// They can have fields annotated with {@code @Unique} to enforce unique constraints on those fields within the storage engine.
public interface Model {

    /// Checks if the model satisfies all unique constraints defined by the {@code @Unique} annotation.
    /// Inside the database, no two models can have the same value for a field marked with {@code @Unique}.
    /// @param engine The storage engine to check against for existing models with the same unique field values.
    /// @return {@code true} if the model satisfies all unique constraints, {@code false} otherwise.
    default <M extends Model, S extends StorageEngine<M>> boolean checkUniqueConstraints(S engine) {

        for (var field : this.getClass().getDeclaredFields()) if (field.isAnnotationPresent(Unique.class)) {

            field.setAccessible(true);
            try {

                var value = field.get(this);
                if (value != null) {

                    var existing = engine.read(m -> {
                        try {

                            var f = m.getClass().getDeclaredField(field.getName());
                            f.setAccessible(true);
                            return value.equals(f.get(m));
                        } catch (NoSuchFieldException | IllegalAccessException e) { return false; }
                    });
                    return !existing.isEmpty();
                }
            } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
        }
        return false;
    }
}
