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
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AllowedValues {

    /// The enum class that provides the allowed values for the annotated field.
    Class<? extends Enum<?>> provider();
}
