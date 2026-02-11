package com.lucaprevioo.jdbi;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/// Annotation to specify the maximum size of a string/array field within the storage engine.
///
/// Fields annotated with {@code @Size} must have values that do not exceed the specified maximum size.
/// When a model is created or updated, the storage engine will check for size constraint violations and
/// throw a {@code RuntimeException} if a violation is detected.
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Size {

    /// Minimum size for the annotated field. Default is 0.
    int min() default 0;

    /// Maximum size for the annotated field. Default is {@code Integer.MAX_VALUE}.
    int max() default Integer.MAX_VALUE;
}
