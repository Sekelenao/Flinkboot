package io.github.sekelenao.flinkboot.test.api.assertion.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    class ConstructorTests {

        @Test
        @DisplayName("Should instantiate successfully with non-null object")
        void shouldInstantiateWithNonNullObject() {
            assertDoesNotThrow(() -> new ObjectAssert<>(new SerializableSample()));
        }

        @Test
        @DisplayName("Should throw NullPointerException when object is null")
        void shouldThrowExceptionWhenObjectIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> new ObjectAssert<>(null));
            assertEquals("Object to assert must not be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("isSerializable")
    class IsSerializableTests {

        @Test
        @DisplayName("Should pass for a serializable object and return same ObjectAssert instance for fluent chaining")
        void shouldPassForSerializableObjectAndReturnThis() {
            var objectAssert = new ObjectAssert<>(new SerializableSample());
            var result = objectAssert.isSerializable();
            assertSame(objectAssert, result);
        }

        @Test
        @DisplayName("Should fail with AssertionFailedError for a non-serializable object")
        void shouldFailForNonSerializableObject() {
            var objectAssert = new ObjectAssert<>(new NonSerializableSample());
            assertThrows(AssertionFailedError.class, objectAssert::isSerializable);
        }
    }
}
