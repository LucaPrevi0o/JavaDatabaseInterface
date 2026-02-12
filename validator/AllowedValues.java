package com.lucaprevioo.jdbi.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/// Annotation to specify that a field's value must be one of a predefined set of allowed values within the storage engine.
///
/// Fields annotated with {@code @AllowedValues} must have values that are defined in the specified enum provider.
/// When a model is created or updated, the storage engine will check for allowed values constraint violations and throw
/// a {@code RuntimeException} if a violation is detected.
///
/// @see Provider
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AllowedValues {

    /// Interface for providing enum values to the storage engine.
    ///
    /// Implementing classes must provide an array of enum values that represent the allowed values for a specific
    /// field annotated with {@code @AllowedValues}. The storage engine will use the provided enum values to enforce
    /// allowed values constraints when creating or updating models.
    ///
    /// @param <M> The type of the enum values provided by this interface.
    interface Provider<M> {

        /// Returns an array of enum values that represent the allowed values for a specific field.
        /// The storage engine will use these values to enforce allowed values constraints when creating or updating models.
        /// @return An array of enum values that represent the allowed values for a specific field.
        M[] getEnumValues();
    }

    /// The enum class that provides the allowed values for the annotated field. The enum must implement the
    /// {@code Provider} interface.
    Class<? extends Provider<?>> provider();
}
