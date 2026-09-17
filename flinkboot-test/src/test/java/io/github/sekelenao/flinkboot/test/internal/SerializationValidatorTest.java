package io.github.sekelenao.flinkboot.test.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SerializationValidator")
class SerializationValidatorTest {

    private SerializationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SerializationValidator();
    }

    static class SerializableSample implements Serializable {
        private final String value = "sample";
    }

    static class NonSerializableSample {
        private final Object value = new Object();
    }

    @Nested
    @DisplayName("validate")
    class ValidateTests {

        @Test
        @DisplayName("Should not throw for a serializable object")
        void shouldNotThrowForSerializableObject() {
            assertDoesNotThrow(() -> validator.validate(new SerializableSample()));
        }

        @Test
        @DisplayName("Should throw AssertionFailedError for a non-serializable object")
        void shouldThrowForNonSerializableObject() {
            var exception = assertThrows(
                    AssertionFailedError.class,
                    () -> validator.validate(new NonSerializableSample())
            );
            assertTrue(exception.getMessage().contains(NonSerializableSample.class.getName()));
        }

        @Test
        @DisplayName("Should throw NullPointerException when target object is null")
        void shouldThrowExceptionWhenObjectIsNull() {
            assertThrows(NullPointerException.class, () -> validator.validate(null));
        }
    }
}
