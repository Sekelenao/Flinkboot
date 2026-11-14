package com.flinkboot.core.internal.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ValidationFeatures")
class ValidationFeaturesTest {

    @Nested
    @DisplayName("Builder")
    class Builder {

        @Test
        @DisplayName("Should build ValidationFeatures with validation disabled and custom capacity")
        void shouldBuildWithValidationDisabled() {
            var features = ValidationFeatures.builder()
                .disableValidation(true)
                .capacity(25)
                .build();

            assertAll(
                () -> assertTrue(features.disableValidation()),
                () -> assertEquals(25, features.capacity())
            );
        }

        @Test
        @DisplayName("Should build ValidationFeatures with validation enabled and minimum positive capacity")
        void shouldBuildWithValidationEnabledAndMinimumCapacity() {
            var features = ValidationFeatures.builder()
                .disableValidation(false)
                .capacity(1)
                .build();

            assertAll(
                () -> assertFalse(features.disableValidation()),
                () -> assertEquals(1, features.capacity())
            );
        }

        @Test
        @DisplayName("Should build ValidationFeatures with maximum capacity")
        void shouldBuildWithMaximumCapacity() {
            var features = ValidationFeatures.builder()
                .disableValidation(false)
                .capacity(Integer.MAX_VALUE)
                .build();

            assertAll(
                () -> assertFalse(features.disableValidation()),
                () -> assertEquals(Integer.MAX_VALUE, features.capacity())
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @ParameterizedTest(name = "capacity={0}")
        @ValueSource(ints = {0, -1, -5, Integer.MIN_VALUE})
        @DisplayName("Should throw IllegalArgumentException when capacity is zero or negative")
        void shouldThrowWhenCapacityIsZeroOrNegative(int invalidCapacity) {
            var exception = assertThrows(IllegalArgumentException.class, () ->
                ValidationFeatures.builder()
                    .disableValidation(false)
                    .capacity(invalidCapacity)
            );

            assertEquals("Validation capacity must be strictly positive", exception.getMessage());
        }
    }
}
