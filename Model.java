package com.lucaprevioo.jdbi;

import com.lucaprevioo.jdbi.validator.*;
import java.lang.reflect.Field;

/// Marker interface for data models.
///
/// Models are plain Java objects that represent data entities in the storage engine.
/// They can have annotated fields to enforce multiple constraints on the data.
public interface Model {

    /// Checks if the model satisfies all unique constraints defined by the {@code @Unique} annotation.
    /// Inside the database, no two models can have the same value for a field marked with {@code @Unique}.
    /// @param engine The storage engine to check against for existing models with the same unique field values.
    /// @return {@code true} if the model satisfies all unique constraints, {@code false} otherwise.
    default <M extends Model, S extends StorageEngine<M>> boolean checkUniqueConstraints(S engine, Field field) {

        field.setAccessible(true);
        try {

            var value = field.get(this);
            if (value != null) return engine.read(m -> {

                try {

                    var declaredField = m.getClass().getDeclaredField(field.getName());
                    declaredField.setAccessible(true);
                    var fieldValue = declaredField.get(m);
                    System.out.println("Comparing unique field value: " + fieldValue + " with " + value);
                    return fieldValue.equals(value);
                } catch (NoSuchFieldException | IllegalAccessException e) { throw new RuntimeException(e); }
            }).isEmpty();
        } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
        return false;
    }

    /// Checks if the model satisfies all not-null constraints defined by the {@code @NotNull} annotation.
    ///
    /// Fields annotated with {@code @NotNull} must have non-null values. This method checks if any of those fields are null.
    /// @return {@code true} if the model satisfies all not-null constraints, {@code false} if any field marked with {@code @NotNull} is null.
    default boolean checkNotNullConstraints(Field field) {

        field.setAccessible(true);
        try { return field.get(this) != null; }
        catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
    }

    /// Checks if the model satisfies all range constraints defined by the {@code @Range} annotation.
    /// Fields annotated with {@code @Range} must have values within the specified range. This method checks if any
    /// of those fields are out of range.
    /// @return {@code true} if the model satisfies all range constraints, {@code false} if any field marked with
    /// {@code @Range} is out of range.
    default boolean checkRangeConstraints(Field field) {

        field.setAccessible(true);
        try {

            var value = field.get(this);
            if (!(value instanceof Number n)) throw new RuntimeException("Field " + field.getName() + " is not a numeric type.");

            var range = field.getAnnotation(Range.class);
            var numValue = n.doubleValue();
            return !(numValue < range.min() || numValue > range.max());
        } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
    }

    /// Checks if the model satisfies all size constraints defined by the {@code @Size} annotation.
    ///
    /// Fields annotated with {@code @Size} must have values that do not exceed the specified maximum size.
    /// This method checks if any of those fields are out of size constraints.
    /// @return {@code true} if the model satisfies all size constraints, {@code false} if any field marked with {@code @Size} is out of size constraints.
    default boolean checkSizeConstraints(Field field) {

        field.setAccessible(true);
        try {

            var value = field.get(this);
            var size = field.getAnnotation(Size.class);
            if (value instanceof String s) return !(s.length() < size.min() || s.length() > size.max());
            else if (value instanceof Object[] arr) return !(arr.length < size.min() || arr.length > size.max());
            else throw new RuntimeException("Field " + field.getName() + " is not a string or array type.");
        } catch (IllegalAccessException e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
    }

    /// Checks if the model satisfies all allowed values constraints defined by the {@code @AllowedValues} annotation.
    ///
    /// Fields annotated with {@code @AllowedValues} must have values that are defined in the specified enum provider.
    /// This method checks if any of those fields have values that are not in the allowed values provided by the enum provider.
    /// @return {@code true} if the model satisfies all allowed values constraints, {@code false} if any field marked
    /// with {@code @AllowedValues} has a value that is not in the allowed values provided by the enum provider.
    default boolean checkAllowedValuesConstraints(Field field) {

        field.setAccessible(true);
        try {

            var value = field.get(this);
            var enumValues = field.getAnnotation(AllowedValues.class).provider().getEnumConstants();

            var fieldType = field.getType();
            var enumType = enumValues.getClass().getComponentType();
            if (!fieldType.equals(enumType)) return false;
            for (var enumValue : enumValues)
                if (enumValue.equals(value)) return true;
            return false;
        } catch (Exception e) { throw new RuntimeException("Failed to access field: " + field.getName(), e); }
    }

    /// Checks if the model satisfies all foreign key constraints defined by the {@code @ForeignKey} annotation.
    ///
    /// Fields annotated with {@code @ForeignKey} must reference a valid model instance of the specified target model class.
    /// This method checks if any of those fields reference a non-existing model instance or if there is a type mismatch
    /// between the foreign key field and the target model's ID field.
    /// @param field The field to check for foreign key constraints.
    /// @return {@code true} if the model satisfies all foreign key constraints, {@code false} if there is a type
    /// mismatch between the foreign key field and the target model's ID field.
    private boolean checkForeignKeyConstraints(Field field) {

        var targetModelClass = field.getAnnotation(ForeignKey.class).targetModel();
        var targetIdField = getIdField(targetModelClass);
        if (targetIdField == null) throw new RuntimeException("Target model " + targetModelClass.getName() + " does not have an ID field");
        return field.getType().equals(targetIdField.getType());
    }

    /// Retrieves the field annotated with {@code @Id} from the specified target model class.
    ///
    /// This method iterates through the declared fields of the target model class and returns the field that is annotated
    /// with {@code @Id}. If no such field is found, it returns {@code null}.
    /// @param targetModel The target model class to search for the ID field.
    /// @return The field annotated with {@code @Id} from the specified target model class, or {@code null} if no such field is found.
    private <T extends Model> Field getIdField(Class<T> targetModel) {

        Field foundField = null;
        for (var field : targetModel.getDeclaredFields()) if (field.isAnnotationPresent(Id.class)) {

            if (foundField != null) throw new RuntimeException("ID constraint violated for model: " + targetModel);
            foundField = field;
        }
        return foundField;
    }

    /// Validates the model against all defined constraints.
    /// This method checks if the model satisfies all constraints defined by the annotations on its fields.
    /// @param engine The storage engine to check against for unique constraints and consistency with other constraints.
    /// @return {@code true} if the model satisfies all constraints
    default <M extends Model, S extends StorageEngine<M>> boolean validate(S engine) {

        for (var field : this.getClass().getDeclaredFields()) {

            if ((field.isAnnotationPresent(Unique.class) || field.isAnnotationPresent(Id.class)) && !checkUniqueConstraints(engine, field))
                throw new RuntimeException("Unique constraint violated for model: " + this);
            if ((field.isAnnotationPresent(NotNull.class) || field.isAnnotationPresent(Id.class)) && !checkNotNullConstraints(field))
                throw new RuntimeException("Not-null constraint violated for model: " + this);
            if (field.isAnnotationPresent(Range.class) && !checkRangeConstraints(field))
                throw new RuntimeException("Range constraint violated for model: " + this);
            if (field.isAnnotationPresent(Size.class) && !checkSizeConstraints(field))
                throw new RuntimeException("Size constraint violated for model: " + this);
            if (field.isAnnotationPresent(AllowedValues.class) && !checkAllowedValuesConstraints(field))
                throw new RuntimeException("Allowed values constraint violated for model: " + this);
            if (field.isAnnotationPresent(ForeignKey.class) && !checkForeignKeyConstraints(field))
                throw new RuntimeException("Foreign key constraint violated for model: " + this);
        }
        return true;
    }
}
