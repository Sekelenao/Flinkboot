package io.github.sekelenao.flinkboot.test.api.assertion.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
 */
public final class ObjectAssert {

    private final Object actual;

    /**
     * Creates a new {@link ObjectAssert} for the given target object.
     *
     * @param actual the object to assert
     * @throws NullPointerException if {@code actual} is {@code null}
     */
    public ObjectAssert(Object actual) {
        this.actual = Objects.requireNonNull(actual, "Object to assert must not be null");
    }

    /**
     * Verifies that the target object can be cleanly serialized and
     * deserialized via standard Java serialization.
     * <p>
     * Performs a round-trip through {@link ObjectOutputStream} and
     * {@link ObjectInputStream} against an in-memory byte buffer. A common
     * failure cause is an unintentionally captured non-serializable field
     * (e.g. a database connection, open file handle, logger instance, or
     * outer class {@code this} reference in a non-static inner class).
     *
     * @return this assertion object for method chaining
     * @throws AssertionError if the object is not serializable
     */
    public ObjectAssert isSerializable() {
        try (
                ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
                ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput)
        ) {
            objectOutput.writeObject(actual);
            objectOutput.flush();

            try (
                    ByteArrayInputStream byteInput = new ByteArrayInputStream(byteOutput.toByteArray());
                    ObjectInputStream objectInput = new ObjectInputStream(byteInput)
            ) {
                objectInput.readObject();
            }
        } catch (Exception exception) {
            throw new AssertionError(
                    "Expected instance of "
                            + actual.getClass().getName()
                            + " to be serializable, but it was not: "
                            + exception.getMessage(),
                    exception
            );
        }
        return this;
    }
}
