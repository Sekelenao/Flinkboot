package io.github.sekelenao.flinkboot.test.api.assertion.type;

import io.github.sekelenao.flinkboot.test.internal.SerializationValidator;

import java.io.NotSerializableException;
import java.util.Objects;

/**
 * Fluent assertion provider for verifying general object properties,
 * such as Java serialization compliance.
 * <p>
 * This is useful for Apache Flink user-defined functions ({@code ProcessFunction},
 * {@code MapFunction}, {@code FilterFunction}, {@code KeySelector}, etc.), which are
 * serialized via standard Java serialization and shipped from the Client/JobManager
 * to remote TaskManagers. Verifying serializability locally avoids a fatal
 * {@link NotSerializableException} at job submission or runtime.
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * @Test
 * void shouldBeSerializable() {
 *     MyCustomProcessFunction function = new MyCustomProcessFunction();
 *     FlinkbootAssertions.assertThat(function)
 *         .isSerializable();
 * }
 * }</pre>
 *
 * @param <T> the type of the object under assertion
 */
public final class ObjectAssert<T> {

    private final T actual;

    /**
     * Creates a new {@link ObjectAssert} for the given target object.
     *
     * @param actual the object to assert
     * @throws NullPointerException if {@code actual} is {@code null}
     */
    public ObjectAssert(T actual) {
        this.actual = Objects.requireNonNull(actual, "Object to assert must not be null");
    }

    /**
     * Verifies that the target object can be cleanly serialized and
     * deserialized via standard Java serialization.
     * <p>
     * Performs a round-trip through an in-memory byte buffer. A common
     * failure cause is an unintentionally captured non-serializable field
     * (e.g. a database connection, open file handle, logger instance, or
     * outer class {@code this} reference in a non-static inner class).
     *
     * @return this assertion object for method chaining
     * @throws AssertionError if the object is not serializable
     */
    public ObjectAssert<T> isSerializable() {
        new SerializationValidator().validate(actual);
        return this;
    }
}
