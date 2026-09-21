package io.github.sekelenao.flinkboot.test.internal.serialization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.InvalidObjectException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SerializationValidator")
class SerializationValidatorTest {

    static class SerializableSample implements Serializable {
        private final String value = "sample";
    }

    static class SerializableWithTransientSample implements Serializable {
        private final String value = "sample";
        private final transient Object nonSerializable = new Object();
    }

    static class NonSerializableSample {
        private final Object value = new Object();
    }

    static class DeserializationFailureSample implements Serializable {
        private void readObject(ObjectInputStream in) throws InvalidObjectException {
            throw new InvalidObjectException("Deserialization rejected");
        }
    }

    @Test
    @DisplayName("Should have private constructor that throws AssertionError to prevent instantiation")
    void shouldPreventInstantiation() throws Exception {
        var constructor = SerializationValidator.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("Should not throw for a serializable object")
        void shouldNotThrowForSerializableObject() {
            assertDoesNotThrow(() -> SerializationValidator.validate(new SerializableSample()));
        }

        @Test
        @DisplayName("Should not throw for a serializable object with transient non-serializable field")
        void shouldNotThrowForSerializableObjectWithTransientField() {
            assertDoesNotThrow(() -> SerializationValidator.validate(new SerializableWithTransientSample()));
        }

        @Test
        @DisplayName("Should throw AssertionFailedError with NotSerializableException cause for non-serializable object")
        void shouldThrowForNonSerializableObject() {
            var exception = assertThrows(
                    AssertionFailedError.class,
                    () -> SerializationValidator.validate(new NonSerializableSample())
            );
            assertAll(
                () -> assertTrue(exception.getMessage().contains(NonSerializableSample.class.getName())),
                () -> assertInstanceOf(NotSerializableException.class, exception.getCause())
            );
        }

        @Test
        @DisplayName("Should throw AssertionFailedError with InvalidObjectException cause when deserialization fails")
        void shouldThrowWhenDeserializationFails() {
            var exception = assertThrows(
                    AssertionFailedError.class,
                    () -> SerializationValidator.validate(new DeserializationFailureSample())
            );
            assertAll(
                () -> assertTrue(exception.getMessage().contains(DeserializationFailureSample.class.getName())),
                () -> assertInstanceOf(InvalidObjectException.class, exception.getCause())
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when target object is null")
        void shouldThrowExceptionWhenObjectIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> SerializationValidator.validate(null));
            assertEquals("Object to validate must not be null", exception.getMessage());
        }
    }
}
