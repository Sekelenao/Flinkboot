package io.github.sekelenao.flinkboot.core.api.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.sekelenao.flinkboot.core.api.properties.execution.ExecutionProperties;
import io.github.sekelenao.flinkboot.core.api.properties.execution.ExecutionRuntimeMode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ExecutionEnvironmentProperties Tests")
class ExecutionEnvironmentPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static ExecutionEnvironmentProperties withProperties(Map<String, String> properties) {
        return new ExecutionEnvironmentProperties(null, null, null, null, null, null, properties);
    }

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should correctly return empty Optionals and empty map when initialized with nulls")
        void shouldReturnEmptyOptionalsWhenInitializedWithNulls() {
            var config = new ExecutionEnvironmentProperties(null, null, null, null, null, null, null);

            assertAll(
                () -> assertTrue(config.execution().isEmpty()),
                () -> assertTrue(config.checkpointing().isEmpty()),
                () -> assertTrue(config.restartStrategy().isEmpty()),
                () -> assertTrue(config.stateBackend().isEmpty()),
                () -> assertTrue(config.savepointRestore().isEmpty()),
                () -> assertTrue(config.localWebUi().isEmpty()),
                () -> assertTrue(config.properties().isEmpty())
            );
        }

        @Test
        @DisplayName("Should return unmodifiable defensive copy of properties map")
        void shouldReturnUnmodifiablePropertiesMap() {
            var mutableMap = new HashMap<String, String>();
            mutableMap.put("key", "value");
            var config = withProperties(mutableMap);

            assertThrows(UnsupportedOperationException.class, () -> config.properties().put("other", "value"));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should fail validation when properties map contains null value")
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
        void shouldFailValidationWhenPropertiesMapContainsNullKey() {
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
            assertTrue(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Deserialization Tests")
    class DeserializationTests {

        @Test
        @DisplayName("Should correctly deserialize from JSON/YAML with properties map")
        void shouldDeserializeWithProperties() throws Exception {
            String json = "{\n" +
                "  \"execution\": {\n" +
                "    \"runtime-mode\": \"STREAMING\",\n" +
                "    \"parallelism\": 4\n" +
                "  },\n" +
                "  \"properties\": {\n" +
                "    \"taskmanager.memory.process.size\": \"2g\",\n" +
                "    \"pipeline.operator-chaining.enabled\": \"true\"\n" +
                "  }\n" +
                "}";

            ExecutionEnvironmentProperties config = mapper.readValue(json, ExecutionEnvironmentProperties.class);

            assertAll(
                () -> assertTrue(config.execution().isPresent()),
                () -> assertEquals(ExecutionRuntimeMode.STREAMING, config.execution().get().runtimeMode().orElseThrow()),
                () -> assertEquals(4, config.execution().get().parallelism().orElseThrow()),
                () -> assertEquals(2, config.properties().size()),
                () -> assertEquals("2g", config.properties().get("taskmanager.memory.process.size")),
                () -> assertEquals("true", config.properties().get("pipeline.operator-chaining.enabled"))
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should respect equals and hashCode contracts")
        void shouldRespectEqualsAndHashCode() {
            var exec = new ExecutionProperties(ExecutionRuntimeMode.STREAMING, 2, 8, Duration.ZERO, Duration.ZERO, false);
            var config1 = new ExecutionEnvironmentProperties(exec, null, null, null, null, null, Map.of("k", "v"));
            var config2 = new ExecutionEnvironmentProperties(exec, null, null, null, null, null, Map.of("k", "v"));
            var config3 = new ExecutionEnvironmentProperties(null, null, null, null, null, null, Map.of("k", "v"));

            assertAll(
                () -> assertEquals(config1, config2),
                () -> assertEquals(config1.hashCode(), config2.hashCode()),
                () -> assertNotEquals(config1, config3),
                () -> assertNotEquals(null, config1),
                () -> assertNotEquals("other", config1),
                () -> assertTrue(config1.toString().contains("ExecutionEnvironmentProperties"))
            );
        }
    }
}
