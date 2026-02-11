package com.lucaprevioo.jdbi;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/// Annotation to mark a field as unique within the storage engine.
///
/// Fields annotated with {@code @Unique} must have unique values across all models in the storage engine.
/// When a model is created or updated, the storage engine will check for unique constraint violations and
/// throw a {@code RuntimeException} if a violation is detected.
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Unique { }
