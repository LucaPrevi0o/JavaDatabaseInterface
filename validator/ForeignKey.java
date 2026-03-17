package com.lucaprevioo.jdbi.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/// Annotation to specify that a field is a foreign key referencing another model within the storage engine.
///
/// This annotation is used to establish relationships between different models in the storage engine, allowing for
/// more complex data structures and enforcing referential integrity.
///
/// Fields annotated with {@code @ForeignKey} must reference a valid model instance of the specified target model class.
/// When a model is created or updated, the storage engine will check for foreign key constraint violations and throw a
/// {@code RuntimeException} if a violation is detected.
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ForeignKey {

    /// The action to take when a referenced model instance is updated or deleted. This parameter specifies how the
    /// storage engine should handle the deletion of a model instance that is referenced by a foreign key. The
    /// available actions are:
    /// - {@code CASCADE}: When a referenced model instance is updated or deleted, all model instances that reference
    /// it will also be deleted.
    /// - {@code RESTRICT}: When a referenced model instance is updated or deleted, the deletion will be prevented if
    /// there are any model instances that reference it.
    /// - {@code SET_NULL}: When a referenced model instance is updated or deleted, all model instances that reference
    /// it will have their foreign key field set to null.
    enum Action { CASCADE, RESTRICT, SET_NULL }

    /// The action to take when a referenced model instance is updated or deleted. This parameter specifies how the
    /// storage engine should handle the deletion of a model instance that is referenced by a foreign key.
    Action onDelete() default Action.RESTRICT;

    /// The action to take when a referenced model instance is updated. This parameter specifies how the storage
    /// engine should handle the update of a model instance that is referenced by a foreign key.
    Action onUpdate() default Action.RESTRICT;
}
