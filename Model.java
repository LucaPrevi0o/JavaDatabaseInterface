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

    /// Checks if the model satisfies all not-null constraints defined by the {@code @NotNull} annotation.
    ///
    /// Fields annotated with {@code @NotNull} must have non-null values. This method checks if any of those fields are null.
    /// @param engine The storage engine to check against (not used in this method but included for consistency with the unique constraints method).
    /// @return {@code true} if the model satisfies all not-null constraints, {@code false} if any field marked with {@code @NotNull} is null.
    default <M extends Model, S extends StorageEngine<M>> boolean checkNotNullConstraints(S engine) {

        for (var field : this.getClass().getDeclaredFields()) if (field.isAnnotationPresent(NotNull.class)) {

            field.setAccessible(true);
            try { return field.get(this) != null; }
            catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
        }
        return true;
    }
}
