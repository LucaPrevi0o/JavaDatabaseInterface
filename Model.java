package com.lucaprevioo.jdbi;

import com.lucaprevioo.jdbi.exception.FailedValidationException;
import com.lucaprevioo.jdbi.validator.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/// Marker interface for data models.
///
/// Models are plain Java objects that represent data entities in the storage engine.
/// They can have annotated fields to enforce multiple constraints on the data.
public interface Model {

    /// Check if the field value is unique in the storage engine.
    /// @param field The field to check for uniqueness.
    /// @param engine The storage engine to check against.
    /// @return true if the field value is unique, false otherwise.
    default <M extends Model, S extends StorageEngine<M>> boolean checkUniqueConstraints(Field field, S engine) {

        var data = engine.read(m -> {

            try {

                field.setAccessible(true);
                var value = field.get(m);
                return value != null && value.equals(field.get(this));
            } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
        });
        return data.isEmpty();
    }

    /// Check if the field value is not null.
    /// @param field The field to check for non-null constraint.
    /// @return true if the field value is not null, false otherwise.
    default boolean checkNotNullConstraints(Field field) {

        try {

            field.setAccessible(true);
            return field.get(this) != null;
        } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
    }

    /// Check if the field value satisfies both unique and non-null constraints.
    /// @param field The field to check for ID constraints.
    /// @param engine The storage engine to check against for uniqueness.
    /// @return true if the field value satisfies both constraints, false otherwise.
    default <M extends Model, S extends StorageEngine<M>> boolean checkIdConstraints(Field field, S engine) {
        return checkUniqueConstraints(field, engine) && checkNotNullConstraints(field);
    }

        /// Check if the field value satisfies size constraints defined by the @Size annotation.
        /// @param field The field to check for size constraints.
        /// @return true if the field value satisfies the size constraints, false otherwise.
    default boolean checkSizeConstraints(Field field) {

        try {

            field.setAccessible(true);
            var value = field.get(this);
            var sizeAnnotation = field.getAnnotation(Size.class);
            if (!(value instanceof String || value instanceof Array)) throw new FailedValidationException("Type mismatch for size constraint on field: " + field.getName());

            if (value instanceof String s) return s.length() >= sizeAnnotation.min() && s.length() <= sizeAnnotation.max();
            return Array.getLength(value) >= sizeAnnotation.min() && Array.getLength(value) <= sizeAnnotation.max();
        } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
    }

    default <M extends Model, S extends StorageEngine<M>> void validate(S engine) throws FailedValidationException {

        for (var field : this.getClass().getDeclaredFields()) {

            if (field.getAnnotation(Unique.class) != null && !checkUniqueConstraints(field, engine))
                throw new FailedValidationException("Unique constraint violation on field: " + this.getClass().getSimpleName() + "." + field.getName());
            if (field.getAnnotation(NotNull.class) != null && !checkNotNullConstraints(field))
                throw new FailedValidationException("Non-null constraint violation on field: " + this.getClass().getSimpleName() + "." + field.getName());
            if (field.getAnnotation(Id.class) != null && !checkIdConstraints(field, engine))
                throw new FailedValidationException("ID constraint violation on field: " + this.getClass().getSimpleName() + "." + field.getName());
            if (field.getAnnotation(Size.class) != null && !checkSizeConstraints(field))
                throw new FailedValidationException("Size constraint violation on field: " + this.getClass().getSimpleName() + "." + field.getName());
        }
    }
}
