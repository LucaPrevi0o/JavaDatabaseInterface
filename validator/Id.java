package com.lucaprevioo.jdbi.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/// Annotation to mark a field as ID within the storage engine.
///
/// This constraint is equivalent to a combination of both {@code @Unique} and {@code @NotNull},
/// but can be used to explicitly indicate that the field serves as the primary identifier for the model.
/// This property can be used for cross-model indexing.
///
/// > Note: Applying both an {@code @Id} annotation and a {@code @Unique} or {@code @NotNull} annotation to the same
/// field is redundant and may lead to unexpected behavior, although it is not properly detected as an error.
/// It is recommended to use only the {@code @Id} annotation for fields that serve as primary identifiers, as it
/// inherently enforces both uniqueness and non-nullability constraints.
///
/// Fields annotated with {@code @Id} must have unique and non-null values across all models in the storage engine.
/// When a model is created or updated, the storage engine will check for constraint violations and
/// throw a {@code RuntimeException} if a violation is detected.
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id { }
