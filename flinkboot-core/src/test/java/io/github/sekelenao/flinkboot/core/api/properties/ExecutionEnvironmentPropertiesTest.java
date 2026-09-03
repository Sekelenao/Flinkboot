package io.github.sekelenao.flinkboot.core.api.properties;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExecutionEnvironmentPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static ExecutionEnvironmentProperties withProperties(Map<String, String> properties) {
        return new ExecutionEnvironmentProperties(null, null, null, null, null, null, properties);
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should fail validation when properties map contains null value.")
        void shouldFailValidationWhenPropertiesMapContainsNullValue() {
            var properties = new HashMap<String, String>();
            properties.put("key1", null);

            Set<ConstraintViolation<ExecutionEnvironmentProperties>> violations = validator.validate(withProperties(properties));

            assertAll(
                    () -> assertFalse(violations.isEmpty()),
                    () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("properties")))
            );


        }

        @Test
        @DisplayName("Should fail validation when properties map contains null key")
        void shouldFailValidationWhenPropertiesHasNullKey() {
            var properties = new HashMap<String, String>();
            properties.put(null, "value");

            Set<ConstraintViolation<ExecutionEnvironmentProperties>> violations = validator.validate(withProperties(properties));
            assertAll(
                    () -> assertFalse(violations.isEmpty()),
                    () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().contains("properties")))
            );
        }

        @Test
        @DisplayName("Should pass validation with a valid properties map")
        void shouldPassValidationWithValidProperties() {
            var properties = Map.of("taskmanager.memory.process.size", "2g");

            Set<ConstraintViolation<ExecutionEnvironmentProperties>> violations = validator.validate(withProperties(properties));
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should pass validation when properties map is null")
        void shouldPassValidationWhenPropertiesIsNull() {
            Set<ConstraintViolation<ExecutionEnvironmentProperties>> violations = validator.validate(withProperties(null));
            assertAll(
                    () -> assertTrue(violations.isEmpty()),
                    () -> assertEquals(0, violations.size())
            );
        }
    }
}
