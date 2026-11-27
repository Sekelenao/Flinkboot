package com.flinkboot.core.internal.validation.properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("PropertyViolation")
class PropertyViolationTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw NullPointerException when property is null")
        void shouldThrowWhenPropertyIsNull() {
            var exception = assertThrows(
                NullPointerException.class,
                () -> new PropertyViolation(null, "message")
            );
            assertEquals("property must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when message is null")
        void shouldThrowWhenMessageIsNull() {
            var exception = assertThrows(
                NullPointerException.class,
                () -> new PropertyViolation("property", null)
            );
            assertEquals("message must not be null", exception.getMessage());
        }
    }
}
