package com.lucaprevioo.jdbi.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/// Annotation to mark a field as having a range constraint within the storage engine.
///
/// Fields annotated with {@code @Range} must have values within the specified range across all models in the storage engine.
/// When a model is created or updated, the storage engine will check for range constraint violations and
/// throw a {@code RuntimeException} if a violation is detected.
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Range {

    /// Minimum value for the annotated field. Default is {@code Double.NEGATIVE_INFINITY}.
    double min() default Double.NEGATIVE_INFINITY;

    /// Maximum value for the annotated field. Default is {@code Double.POSITIVE_INFINITY}.
    double max() default Double.POSITIVE_INFINITY;
}
