package io.github.sekelenao.flinkboot.core.api.configuration.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ExecutionConfiguration Tests")
class ExecutionConfigurationTest {

    private static Validator validator;
    private static ObjectMapper mapper;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        mapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should correctly return empty Optionals when initialized with nulls")
        void shouldReturnEmptyOptionals() {
            var config = new ExecutionConfiguration(null, null, null, null, null, null);

            assertAll(
                () -> assertTrue(config.runtimeMode().isEmpty()),
                () -> assertTrue(config.parallelism().isEmpty()),
                () -> assertTrue(config.maxParallelism().isEmpty()),
                () -> assertTrue(config.bufferTimeoutMs().isEmpty()),
                () -> assertTrue(config.autoWatermarkIntervalMs().isEmpty()),
                () -> assertTrue(config.objectReuse().isEmpty())
            );
        }

        @Test
        @DisplayName("Should correctly return present Optionals when initialized with values")
        void shouldReturnPresentOptionals() {
            var config = new ExecutionConfiguration(
                ExecutionRuntimeMode.STREAMING,
                8,
                128,
                100L,
                200L,
                true
            );

            assertAll(
                () -> assertEquals(ExecutionRuntimeMode.STREAMING, config.runtimeMode().orElseThrow()),
                () -> assertEquals(8, config.parallelism().orElseThrow()),
                () -> assertEquals(128, config.maxParallelism().orElseThrow()),
                () -> assertEquals(100L, config.bufferTimeoutMs().orElseThrow()),
                () -> assertEquals(200L, config.autoWatermarkIntervalMs().orElseThrow()),
                () -> assertTrue(config.objectReuse().orElseThrow())
            );
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation when all fields are valid or null")
        void shouldPassValidationWithValidValues() {
            var config = new ExecutionConfiguration(
                ExecutionRuntimeMode.BATCH,
                4,
                64,
                0L,
                0L,
                false
            );

            Set<ConstraintViolation<ExecutionConfiguration>> violations = validator.validate(config);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when parallelism is non-positive")
        void shouldFailValidationWithInvalidParallelism() {
            var config = new ExecutionConfiguration(
                ExecutionRuntimeMode.STREAMING,
                0,
                -1,
                -10L,
                -5L,
                true
            );

            Set<ConstraintViolation<ExecutionConfiguration>> violations = validator.validate(config);
            assertEquals(4, violations.size());
        }
    }

    @Nested
    @DisplayName("Deserialization Tests")
    class DeserializationTests {

        @Test
        @DisplayName("Should correctly deserialize from JSON/YAML kebab-case properties")
        void shouldDeserializeFromKebabCase() throws Exception {
            String json = "{\n" +
                "  \"runtime-mode\": \"STREAMING\",\n" +
                "  \"parallelism\": 16,\n" +
                "  \"max-parallelism\": 256,\n" +
                "  \"buffer-timeout-ms\": 50,\n" +
                "  \"auto-watermark-interval-ms\": 100,\n" +
                "  \"object-reuse\": true\n" +
                "}";

            ExecutionConfiguration config = mapper.readValue(json, ExecutionConfiguration.class);

            assertAll(
                () -> assertEquals(ExecutionRuntimeMode.STREAMING, config.runtimeMode().orElseThrow()),
                () -> assertEquals(16, config.parallelism().orElseThrow()),
                () -> assertEquals(256, config.maxParallelism().orElseThrow()),
                () -> assertEquals(50L, config.bufferTimeoutMs().orElseThrow()),
                () -> assertEquals(100L, config.autoWatermarkIntervalMs().orElseThrow()),
                () -> assertTrue(config.objectReuse().orElseThrow())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should respect equals and hashCode contracts")
        void shouldRespectEqualsAndHashCode() {
            var config1 = new ExecutionConfiguration(ExecutionRuntimeMode.STREAMING, 8, 128, 100L, 200L, true);
            var config2 = new ExecutionConfiguration(ExecutionRuntimeMode.STREAMING, 8, 128, 100L, 200L, true);
            var config3 = new ExecutionConfiguration(ExecutionRuntimeMode.BATCH, 4, 64, 50L, 100L, false);

            assertAll(
                () -> assertEquals(config1, config2),
                () -> assertEquals(config1.hashCode(), config2.hashCode()),
                () -> assertNotEquals(config1, config3),
                () -> assertNotEquals(null, config1),
                () -> assertNotEquals("string", config1)
            );
        }
    }
}
