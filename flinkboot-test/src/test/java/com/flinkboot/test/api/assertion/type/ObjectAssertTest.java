package com.flinkboot.test.api.assertion.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.NotSerializableException;
import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectAssert")
class ObjectAssertTest {

    static class SerializableSample implements Serializable {
        private final String value = "sample";
    }

    static class NonSerializableSample {
        private final Object value = new Object();
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw NullPointerException when object is null")
        void shouldThrowExceptionWhenObjectIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> new ObjectAssert<>(null));
            assertEquals("Object to assert must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("isSerializable")
    class IsSerializable {

        @Test
        @DisplayName("Should pass for a serializable object and return same ObjectAssert instance for fluent chaining")
        void shouldPassForSerializableObjectAndReturnThis() {
            var objectAssert = new ObjectAssert<>(new SerializableSample());
            var result = objectAssert.isSerializable();
            assertSame(objectAssert, result);
        }

        @Test
        @DisplayName("Should fail with AssertionFailedError and NotSerializableException cause for non-serializable object")
        void shouldFailForNonSerializableObject() {
            var objectAssert = new ObjectAssert<>(new NonSerializableSample());
            var exception = assertThrows(AssertionFailedError.class, objectAssert::isSerializable);
            assertAll(
                () -> assertTrue(exception.getMessage().contains(NonSerializableSample.class.getName())),
                () -> assertInstanceOf(NotSerializableException.class, exception.getCause())
            );
        }
    }
}
