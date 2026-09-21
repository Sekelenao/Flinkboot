package io.github.sekelenao.flinkboot.test.internal.serialization;

import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

public final class SerializationValidator {

    private SerializationValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    private static byte[] serialize(Object actual) throws IOException {
        var byteOutput = new ByteArrayOutputStream();
        try (var objectOutput = new ObjectOutputStream(byteOutput)) {
            objectOutput.writeObject(actual);
            objectOutput.flush();
            return byteOutput.toByteArray();
        }
    }

    private static void deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (var objectInput = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            objectInput.readObject();
        }
    }

    public static void validate(Object actual) {
        Objects.requireNonNull(actual, "Object to validate must not be null");
        try {
            deserialize(serialize(actual));
        } catch (Exception exception) {
            Assertions.fail("Expected instance of " + actual.getClass().getName() + " to be serializable", exception);
        }
    }
}
