package io.github.sekelenao.flinkboot.core.api.validation;

import io.github.sekelenao.flinkboot.core.internal.validation.ConfigurationValidator;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.ConfigurationValidationException;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ValidatableProperties")
class ValidatablePropertiesTest {

    static class ValidObject implements ValidatableProperties {
        @Override
        public boolean validate(ConstraintValidatorContext context) {
            return true;
        }
    }

    static class InvalidObjectWithCustomProperty implements ValidatableProperties {
        @Override
        public boolean validate(ConstraintValidatorContext context) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("custom violation on specific node")
                   .addPropertyNode("customField")
                   .addConstraintViolation();
            return false;
        }
    }

    static class InvalidObjectDefaultViolation implements ValidatableProperties {
        @Override
        public boolean validate(ConstraintValidatorContext context) {
            return false;
        }
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("Should pass validation when validate() returns true")
        void shouldPassWhenValid() {
            try (var validator = new ConfigurationValidator(10)) {
                assertDoesNotThrow(() -> validator.validate(new ValidObject()));
            }
        }

        @Test
        @DisplayName("Should fail validation and attach violation to custom property node")
        void shouldFailWithCustomPropertyNode() {
            try (var validator = new ConfigurationValidator(10)) {
                var ex = assertThrows(ConfigurationValidationException.class, () -> validator.validate(new InvalidObjectWithCustomProperty()));
                assertAll(
                    () -> assertTrue(ex.getMessage().contains("customField:")),
                    () -> assertTrue(ex.getMessage().contains("custom violation on specific node"))
                );
            }
        }

        @Test
        @DisplayName("Should fail validation with default template when no custom node added")
        void shouldFailWithDefaultViolation() {
            try (var validator = new ConfigurationValidator(10)) {
                var ex = assertThrows(ConfigurationValidationException.class, () -> validator.validate(new InvalidObjectDefaultViolation()));
                assertTrue(ex.getMessage().contains("Invalid configuration properties"));
            }
        }
    }
}
