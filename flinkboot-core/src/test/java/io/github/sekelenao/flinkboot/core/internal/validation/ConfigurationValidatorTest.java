package io.github.sekelenao.flinkboot.core.internal.validation;

import io.github.sekelenao.flinkboot.core.api.exception.configuration.ConfigurationValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ConfigurationValidator")
class ConfigurationValidatorTest {

    public static class ValidConfig {
        @NotBlank
        public String name = "valid";

        @Positive
        public int count = 5;
    }

    public static class SingleViolationConfig {
        @NotBlank
        public String name = "";
    }

    public static class FourViolationsConfig {
        @NotBlank
        public String alpha = "";

        @NotEmpty
        public List<String> beta = List.of();

        @Positive
        public int gamma = -1;

        @NotNull
        public String delta = null;
    }

    public static class TwelveViolationsConfig {
        @NotBlank public String f01 = "";
        @NotBlank public String f02 = "";
        @NotBlank public String f03 = "";
        @NotBlank public String f04 = "";
        @NotBlank public String f05 = "";
        @NotBlank public String f06 = "";
        @NotBlank public String f07 = "";
        @NotBlank public String f08 = "";
        @NotBlank public String f09 = "";
        @NotBlank public String f10 = "";
        @NotBlank public String f11 = "";
        @NotBlank public String f12 = "";
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Should successfully instantiate when capacity is positive")
        void shouldInstantiateWhenCapacityIsPositive() {
            assertDoesNotThrow(() -> {
                try (var validator = new ConfigurationValidator(5)) {
                    // instantiated
                }
            });
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when capacity is zero")
        void shouldThrowWhenCapacityIsZero() {
            var exception = assertThrows(IllegalArgumentException.class, () -> new ConfigurationValidator(0));
            assertEquals("Capacity must be strictly positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when capacity is negative")
        void shouldThrowWhenCapacityIsNegative() {
            var exception = assertThrows(IllegalArgumentException.class, () -> new ConfigurationValidator(-3));
            assertEquals("Capacity must be strictly positive", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Validation")
    class ValidationTests {

        @Test
        @DisplayName("Should throw NullPointerException when configuration is null")
        void shouldThrowWhenConfigurationIsNull() {
            try (var validator = new ConfigurationValidator(10)) {
                assertThrows(NullPointerException.class, () -> validator.validate(null));
            }
        }

        @Test
        @DisplayName("Should pass without exception when configuration has no violations")
        void shouldPassWhenNoViolations() {
            try (var validator = new ConfigurationValidator(10)) {
                assertDoesNotThrow(() -> validator.validate(new ValidConfig()));
            }
        }

        @Test
        @DisplayName("Should throw ConfigurationValidationException with formatted message for single violation")
        void shouldThrowWithFormattedMessageForSingleViolation() {
            try (var validator = new ConfigurationValidator(10)) {
                var exception = assertThrows(ConfigurationValidationException.class, () -> validator.validate(new SingleViolationConfig()));
                var message = exception.getMessage();

                assertAll(
                    () -> assertTrue(message.startsWith("Configuration validation failed with 1 violation(s):\n - ")),
                    () -> assertTrue(message.contains("name:"))
                );
            }
        }

        @Test
        @DisplayName("Should throw ConfigurationValidationException and list all violations when below capacity")
        void shouldListAllViolationsWhenBelowCapacity() {
            try (var validator = new ConfigurationValidator(10)) {
                var exception = assertThrows(ConfigurationValidationException.class, () -> validator.validate(new FourViolationsConfig()));
                var message = exception.getMessage();

                assertAll(
                    () -> assertTrue(message.startsWith("Configuration validation failed with 4 violation(s):\n - ")),
                    () -> assertTrue(message.contains("alpha:")),
                    () -> assertTrue(message.contains("beta:")),
                    () -> assertTrue(message.contains("delta:")),
                    () -> assertTrue(message.contains("gamma:"))
                );
            }
        }

        @Test
        @DisplayName("Should truncate and append overflow suffix when violations exceed capacity")
        void shouldTruncateAndAppendSuffixWhenExceedingCapacity() {
            try (var validator = new ConfigurationValidator(10)) {
                var exception = assertThrows(ConfigurationValidationException.class, () -> validator.validate(new TwelveViolationsConfig()));
                var message = exception.getMessage();

                assertAll(
                    () -> assertTrue(message.startsWith("Configuration validation failed with 12 violation(s):\n - ")),
                    () -> assertTrue(message.contains("f01:")),
                    () -> assertTrue(message.contains("f10:")),
                    () -> assertTrue(message.contains(" - ... and 2 more violation(s)"))
                );
            }
        }
    }

    @Nested
    @DisplayName("Lifecycle")
    class LifecycleTests {

        @Test
        @DisplayName("Should close validator factory gracefully")
        void shouldCloseGracefully() {
            var validator = new ConfigurationValidator(10);
            assertDoesNotThrow(validator::close);
        }
    }
}
