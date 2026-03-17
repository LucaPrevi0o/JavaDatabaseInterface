package com.lucaprevioo.jdbi;

import com.lucaprevioo.jdbi.engine.EngineRegistry;
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
    /// @return {@code true} if the field value is unique, {@code false} otherwise.
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
    /// @return {@code true} if the field value is not null, {@code false} otherwise.
    default boolean checkNotNullConstraints(Field field) {

        try {

            field.setAccessible(true);
            return field.get(this) != null;
        } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
    }

    /// Check if the field value satisfies both unique and non-null constraints.
    /// @param field The field to check for ID constraints.
    /// @param engine The storage engine to check against for uniqueness.
    /// @return {@code true} if the field value satisfies both unique and non-null constraints, {@code false} otherwise.
    default <M extends Model, S extends StorageEngine<M>> boolean checkIdConstraints(Field field, S engine) {
        return checkUniqueConstraints(field, engine) && checkNotNullConstraints(field);
    }

    /// Check if the field value satisfies size constraints defined by the {@code @Size} annotation.
    /// @param field The field to check for size constraints.
    /// @return {@code true} if the field value satisfies the size constraints, {@code false} otherwise.
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

    /// Check if the field value satisfies range constraints defined by the {@code @Range} annotation.
    /// @param field The field to check for range constraints.
    /// @return {@code true} if the field value satisfies the range constraints, {@code false} otherwise.
    default boolean checkRangeConstraints(Field field) {

        try {

            field.setAccessible(true);
            var value = field.get(this);
            var rangeAnnotation = field.getAnnotation(Range.class);
            if (!(value instanceof Number)) throw new FailedValidationException("Type mismatch for range constraint on field: " + field.getName());

            var numValue = ((Number) value).doubleValue();
            return numValue >= rangeAnnotation.min() && numValue <= rangeAnnotation.max();
        } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
    }

    /// Check if the field value is among the allowed values defined by the {@code @AllowedValues} annotation.
    /// @param field The field to check for allowed values constraints.
    /// @return {@code true} if the field value is among the allowed values, {@code false} otherwise.
    default boolean checkAllowedValuesConstraints(Field field) {

        try {

            field.setAccessible(true);
            var value = field.get(this);
            var allowedValues = field.getAnnotation(AllowedValues.class).provider().getEnumConstants();
            if (allowedValues == null) throw new FailedValidationException("Allowed values provider must be an enum type for field: " + field.getName());
            for (var allowedValue : allowedValues) if (allowedValue.equals(value)) return true;
            return false;
        } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
    }

    /// Get the field annotated with {@code @Id} in the model class.
    /// @param modelClass The model instance to inspect for the ID field.
    /// @return The field annotated with {@code @Id}, or {@code null} if no such field exists.
    default Field getIdField(Class<?> modelClass) {

        for (var field : modelClass.getDeclaredFields())
            if (field.getAnnotation(Id.class) != null) return field;
        return null;
    }

    /// Check if the field value satisfies foreign key constraints defined by the {@code @ForeignKey} annotation.
    ///
    /// This method checks if the value of the foreign key field exists in the target storage engine.
    /// It retrieves the target model's ID field and compares it against the values in the target engine to ensure
    /// referential integrity.
    /// @param field The field to check for foreign key constraints.
    /// @param registry The engine registry to access the target storage engine.
    /// @return {@code true} if the field value satisfies foreign key constraints, {@code false} otherwise.
    default boolean checkForeignKeyConstraints(Field field, EngineRegistry registry) {

        try {

            field.setAccessible(true);
            var type = field.getType();
            var value = field.get(this);
            var targetIdField = getIdField(type);
            if (targetIdField == null) throw new FailedValidationException("Foreign key target model must have an ID field: " + field.getName());

            var targetEngine = registry.get(type);
            return !targetEngine.read(m -> {

                try {

                    targetIdField.setAccessible(true);
                    var targetIdValue = targetIdField.get(m);
                    var valueIdValue = targetIdField.get(value);
                    return valueIdValue != null && valueIdValue.equals(targetIdValue);
                } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + targetIdField.getName(), e);}
            }).isEmpty();
        } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
    }

    /// Validate the model instance against all defined constraints on its fields.
    /// This method iterates through all declared fields of the model class and checks for various constraints based on
    /// the annotations present on each field. If any constraint is violated, a {@code FailedValidationException} is
    /// thrown with a descriptive message.
    /// @param engine The storage engine to check against for constraints that require access to existing data (e.g.,
    /// unique constraints).
    /// @param registry The engine registry to access other storage engines for constraints that involve relationship
    /// (e.g., foreign key constraints).
    /// @throws FailedValidationException if any constraint is violated, with a message indicating the specific field
    /// and type of violation.
    default <M extends Model, S extends StorageEngine<M>> void validate(S engine, EngineRegistry registry) throws FailedValidationException {

        for (var field : this.getClass().getDeclaredFields()) {

            if (field.getAnnotation(Unique.class) != null && !checkUniqueConstraints(field, engine))
                throw new FailedValidationException("Unique constraint violation on field: " + this.getClass().getSimpleName() + "." + field.getName());
            if (field.getAnnotation(NotNull.class) != null && !checkNotNullConstraints(field))
                throw new FailedValidationException("Non-null constraint violation on field: " + this.getClass().getSimpleName() + "." + field.getName());
            if (field.getAnnotation(Id.class) != null && !checkIdConstraints(field, engine))
                throw new FailedValidationException("ID constraint violation on field: " + this.getClass().getSimpleName() + "." + field.getName());
            if (field.getAnnotation(Size.class) != null && !checkSizeConstraints(field))
                throw new FailedValidationException("Size constraint violation on field: " + this.getClass().getSimpleName() + "." + field.getName());
            if (field.getAnnotation(Range.class) != null && !checkRangeConstraints(field))
                throw new FailedValidationException("Range constraint violation on field: " + this.getClass().getSimpleName() + "." + field.getName());
            if (field.getAnnotation(AllowedValues.class) != null && !checkAllowedValuesConstraints(field))
                throw new FailedValidationException("Allowed values constraint violation on field: " + this.getClass().getSimpleName() + "." + field.getName());
            if (field.getAnnotation(ForeignKey.class) != null && !checkForeignKeyConstraints(field, registry))
                throw new FailedValidationException("Foreign key constraint violation on field: " + this.getClass().getSimpleName() + "." + field.getName());
        }
    }
}
