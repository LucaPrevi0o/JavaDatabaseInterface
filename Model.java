package com.lucaprevioo.jdbi;

/// Marker interface for data models.
///
/// Models are plain Java objects that represent data entities in the storage engine.
/// They can have annotated fields to enforce multiple constraints on the data.
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
    /// @return {@code true} if the model satisfies all not-null constraints, {@code false} if any field marked with {@code @NotNull} is null.
    default <M extends Model, S extends StorageEngine<M>> boolean checkNotNullConstraints() {

        for (var field : this.getClass().getDeclaredFields()) if (field.isAnnotationPresent(NotNull.class)) {

            field.setAccessible(true);
            try { return field.get(this) != null; }
            catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
        }
        return true;
    }

    /// Checks if the model satisfies all range constraints defined by the {@code @Range} annotation.
    /// Fields annotated with {@code @Range} must have values within the specified range. This method checks if any
    /// of those fields are out of range.
    /// @return {@code true} if the model satisfies all range constraints, {@code false} if any field marked with
    /// {@code @Range} is out of range.
    default <M extends Model, S extends StorageEngine<M>> boolean checkRangeConstraints() {

        for (var field : this.getClass().getDeclaredFields()) if (field.isAnnotationPresent(Range.class)) {

            field.setAccessible(true);
            try {

                var value = field.get(this);
                if (!(value instanceof Number n)) throw new RuntimeException("Field " + field.getName() + " is not a numeric type.");

                var range = field.getAnnotation(Range.class);
                var numValue = n.doubleValue();
                return !(numValue < range.min() || numValue > range.max());
            } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
        }
        return true;
    }

    /// Checks if the model satisfies all size constraints defined by the {@code @Size} annotation.
    ///
    /// Fields annotated with {@code @Size} must have values that do not exceed the specified maximum size.
    /// This method checks if any of those fields are out of size constraints.
    /// @return {@code true} if the model satisfies all size constraints, {@code false} if any field marked with {@code @Size} is out of size constraints.
    default <M extends Model, S extends StorageEngine<M>> boolean checkSizeConstraints() {

        for (var field : this.getClass().getDeclaredFields()) if (field.isAnnotationPresent(Size.class)) {

            field.setAccessible(true);
            try {

                var value = field.get(this);
                var size = field.getAnnotation(Size.class);
                if (value instanceof String s) return !(s.length() < size.min() || s.length() > size.max());
                else if (value instanceof Object[] arr) return !(arr.length < size.min() || arr.length > size.max());
                else throw new RuntimeException("Field " + field.getName() + " is not a string or array type.");
            } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
        }
        return true;
    }

    /// Validates the model against all defined constraints (unique, not-null, range, size).
    /// This method checks if the model satisfies all constraints defined by the annotations on its fields.
    /// @param engine The storage engine to check against for unique constraints and consistency with other constraints.
    /// @return {@code true} if the model satisfies all constraints, {@code false} if any constraint is violated.
    default <M extends Model, S extends StorageEngine<M>> boolean validate(S engine) {
        return !checkUniqueConstraints(engine) || !checkNotNullConstraints() || !checkRangeConstraints() || !checkSizeConstraints();
    }
}
