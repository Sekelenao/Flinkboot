package io.github.sekelenao.flinkboot.test.api.assertion;

import io.github.sekelenao.flinkboot.test.api.assertion.type.ClassAssert;

import java.util.Objects;


/**
 * Entry point for Flinkboot fluent assertions.
 * <p>
 * Provides factory methods to instantiate dedicated assert objects for validating
 * Apache Flink POJO serialization compliance and data structures.
 *
 * <h3>Example: Asserting POJO Compliance</h3>
 * <pre>{@code
 * @Test
 * void shouldBePojoCompliant() {
 *     FlinkbootAssertions.assertThat(UserActivity.class)
 *         .isPojo();
 * }
 * }</pre>
 */
public final class FlinkbootAssertions {

    private FlinkbootAssertions() {
        throw new AssertionError("You cannot instantiate this class");
    }

    /**
     * Creates a new instance of {@link ClassAssert} to assert on the specified class.
     *
     * @param type the target class to assert
     * @return the created {@link ClassAssert} assertion object
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static ClassAssert assertThat(Class<?> type) {
        Objects.requireNonNull(type, "Class to assert must not be null");
        return new ClassAssert(type);
    }

}
