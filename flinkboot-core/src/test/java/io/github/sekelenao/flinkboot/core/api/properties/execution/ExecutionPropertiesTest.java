package io.github.sekelenao.flinkboot.core.api.properties.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ExecutionProperties Tests")
class ExecutionPropertiesTest {

    private static Validator validator;
    private static ObjectMapper mapper;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should correctly return empty Optionals when initialized with nulls")
        void shouldReturnEmptyOptionals() {
            var config = new ExecutionProperties(null, null, null, null, null, null);

            assertAll(
                () -> assertTrue(config.runtimeMode().isEmpty()),
                () -> assertTrue(config.parallelism().isEmpty()),
                () -> assertTrue(config.maxParallelism().isEmpty()),
                () -> assertTrue(config.bufferTimeout().isEmpty()),
                () -> assertTrue(config.autoWatermarkInterval().isEmpty()),
                () -> assertTrue(config.objectReuse().isEmpty())
            );
        }

        @Test
        @DisplayName("Should correctly return present Optionals when initialized with values")
        void shouldReturnPresentOptionals() {
            var config = new ExecutionProperties(
                ExecutionRuntimeMode.STREAMING,
                8,
                128,
                Duration.ofMillis(100),
                Duration.ofMillis(200),
                true
            );

            assertAll(
                () -> assertEquals(ExecutionRuntimeMode.STREAMING, config.runtimeMode().orElseThrow()),
                () -> assertEquals(8, config.parallelism().orElseThrow()),
                () -> assertEquals(128, config.maxParallelism().orElseThrow()),
                () -> assertEquals(Duration.ofMillis(100), config.bufferTimeout().orElseThrow()),
                () -> assertEquals(Duration.ofMillis(200), config.autoWatermarkInterval().orElseThrow()),
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
            var config = new ExecutionProperties(
                ExecutionRuntimeMode.BATCH,
                4,
                64,
                Duration.ZERO,
                Duration.ZERO,
                false
            );

            Set<ConstraintViolation<ExecutionProperties>> violations = validator.validate(config);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when parallelism is non-positive")
        void shouldFailValidationWithInvalidParallelism() {
            var config = new ExecutionProperties(
                ExecutionRuntimeMode.STREAMING,
                0,
                -1,
                Duration.ofMillis(100),
                Duration.ofMillis(200),
                true
            );

            Set<ConstraintViolation<ExecutionProperties>> violations = validator.validate(config);
            assertEquals(2, violations.size());
        }

        @Test
        @DisplayName("Should fail validation when durations are negative")
        void shouldFailValidationWithNegativeDurations() {
            var config = new ExecutionProperties(
                ExecutionRuntimeMode.STREAMING,
                1,
                1,
                Duration.ofSeconds(-1),
                Duration.ofSeconds(-2),
                true
            );

            Set<ConstraintViolation<ExecutionProperties>> violations = validator.validate(config);
            assertEquals(2, violations.size());
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
                "  \"buffer-timeout\": \"PT0.05S\",\n" +
                "  \"auto-watermark-interval\": \"PT0.1S\",\n" +
                "  \"object-reuse\": true\n" +
                "}";

            ExecutionProperties config = mapper.readValue(json, ExecutionProperties.class);

            assertAll(
                () -> assertEquals(ExecutionRuntimeMode.STREAMING, config.runtimeMode().orElseThrow()),
                () -> assertEquals(16, config.parallelism().orElseThrow()),
                () -> assertEquals(256, config.maxParallelism().orElseThrow()),
                () -> assertEquals(Duration.ofMillis(50), config.bufferTimeout().orElseThrow()),
                () -> assertEquals(Duration.ofMillis(100), config.autoWatermarkInterval().orElseThrow()),
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
            var config1 = new ExecutionProperties(ExecutionRuntimeMode.STREAMING, 8, 128, Duration.ofMillis(100), Duration.ofMillis(200), true);
            var config2 = new ExecutionProperties(ExecutionRuntimeMode.STREAMING, 8, 128, Duration.ofMillis(100), Duration.ofMillis(200), true);
            var config3 = new ExecutionProperties(ExecutionRuntimeMode.BATCH, 4, 64, Duration.ofMillis(50), Duration.ofMillis(100), false);

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

