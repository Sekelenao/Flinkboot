package io.github.sekelenao.internal.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the visibility of the annotated element has been relaxed
 * (e.g., package-private instead of private) solely to make it accessible from tests.
 * It must not be used from production code outside its declaring class.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
public @interface VisibleForTesting {
}