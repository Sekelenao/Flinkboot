package io.github.sekelenao.flinkboot.test.internal;

import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

public final class SerializationValidator {

    public void validate(Object actual) {
        Objects.requireNonNull(actual, "Object to validate must not be null");
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
            Assertions.fail(
                    "Expected instance of "
                            + actual.getClass().getName()
                            + " to be serializable, but it was not: "
                            + exception.getMessage(),
                    exception
            );
        }
    }
}
