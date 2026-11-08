package com.flinkboot.core.api.properties.restart;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.flinkboot.core.internal.validation.Validators;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ExponentialDelayRestartProperties")
class ExponentialDelayRestartPropertiesTest {

    private static final YAMLMapper mapper = YAMLMapper.builder()
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .findAndAddModules()
        .build();

    private static final Validator validator;

    static {
        try (var factory = Validators.factory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should instantiate with valid arguments")
        void shouldInstantiateWithValidArguments() {
            var config = new ExponentialDelayRestartProperties(
                Duration.ofSeconds(1),
                Duration.ofMinutes(1),
                2.0,
                Duration.ofHours(1),
                0.1,
                5
            );
            assertNotNull(config);
        }

        @Test
        @DisplayName("Should instantiate with null arguments")
        void shouldInstantiateWithNullArguments() {
            var config = new ExponentialDelayRestartProperties(null, null, null, null, null, null);
            assertNotNull(config);
        }
    }

    @Nested
    @DisplayName("Getters")
    class Getters {

        @Test
        @DisplayName("Should return expected values when fields are provided")
        void shouldReturnExpectedValuesWhenFieldsAreProvided() {
            var config = new ExponentialDelayRestartProperties(
                Duration.ofSeconds(2),
                Duration.ofMinutes(2),
                1.5,
                Duration.ofMinutes(30),
                0.2,
                10
            );

            assertAll(
                () -> assertTrue(config.initialBackoff().isPresent()),
                () -> assertEquals(Duration.ofSeconds(2), config.initialBackoff().get()),
                () -> assertTrue(config.maxBackoff().isPresent()),
                () -> assertEquals(Duration.ofMinutes(2), config.maxBackoff().get()),
                () -> assertTrue(config.backoffMultiplier().isPresent()),
                () -> assertEquals(1.5, config.backoffMultiplier().getAsDouble()),
                () -> assertTrue(config.resetBackoffThreshold().isPresent()),
                () -> assertEquals(Duration.ofMinutes(30), config.resetBackoffThreshold().get()),
                () -> assertTrue(config.jitterFactor().isPresent()),
                () -> assertEquals(0.2, config.jitterFactor().getAsDouble()),
                () -> assertTrue(config.attempts().isPresent()),
                () -> assertEquals(10, config.attempts().getAsInt())
            );
        }

        @Test
        @DisplayName("Should return empty optionals when fields are null")
        void shouldReturnEmptyOptionalsWhenFieldsAreNull() {
            var config = new ExponentialDelayRestartProperties(null, null, null, null, null, null);

            assertAll(
                () -> assertTrue(config.initialBackoff().isEmpty()),
                () -> assertTrue(config.maxBackoff().isEmpty()),
                () -> assertTrue(config.backoffMultiplier().isEmpty()),
                () -> assertTrue(config.resetBackoffThreshold().isEmpty()),
                () -> assertTrue(config.jitterFactor().isEmpty()),
                () -> assertTrue(config.attempts().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should pass validation with valid full configuration")
        void shouldPassValidationWithValidConfiguration() {
            var config = new ExponentialDelayRestartProperties(
                Duration.ofSeconds(1),
                Duration.ofMinutes(1),
                2.0,
                Duration.ofHours(1),
                0.1,
                5
            );
            var violations = validator.validate(config);
            assertTrue(violations.isEmpty(), "Expected no violations for valid configuration");
        }

        @Test
        @DisplayName("Should pass validation when all fields are null")
        void shouldPassValidationWhenFieldsAreNull() {
            var config = new ExponentialDelayRestartProperties(null, null, null, null, null, null);
            var violations = validator.validate(config);
            assertTrue(violations.isEmpty(), "Expected no violations for null optional configuration");
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 100})
        @DisplayName("Should pass validation when attempts is positive")
        void shouldPassValidationWhenAttemptsIsPositive(int attempts) {
            var config = new ExponentialDelayRestartProperties(
                Duration.ofSeconds(1),
                Duration.ofMinutes(1),
                2.0,
                Duration.ofHours(1),
                0.1,
                attempts
            );
            var violations = validator.validate(config);
            assertTrue(violations.isEmpty(), "Expected no violations for positive attempts");
        }

        @ParameterizedTest
        @ValueSource(ints = {-10, -1, 0})
        @DisplayName("Should fail validation when attempts is non-positive")
        void shouldFailValidationWhenAttemptsIsNonPositive(int attempts) {
            var config = new ExponentialDelayRestartProperties(
                Duration.ofSeconds(1),
                Duration.ofMinutes(1),
                2.0,
                Duration.ofHours(1),
                0.1,
                attempts
            );
            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("attempts")))
            );
        }

        @Test
        @DisplayName("Should fail validation when backoffMultiplier is less than or equal to 1.0")
        void shouldFailValidationWhenBackoffMultiplierIsLessOrEqualToOne() {
            var config = new ExponentialDelayRestartProperties(
                Duration.ofSeconds(1),
                Duration.ofMinutes(1),
                1.0,
                Duration.ofHours(1),
                0.1,
                3
            );
            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("backoff-multiplier")))
            );
        }

        @Test
        @DisplayName("Should fail validation when jitterFactor is outside [0.0, 1.0]")
        void shouldFailValidationWhenJitterFactorIsOutsideBounds() {
            var config = new ExponentialDelayRestartProperties(
                Duration.ofSeconds(1),
                Duration.ofMinutes(1),
                2.0,
                Duration.ofHours(1),
                1.5,
                3
            );
            var violations = validator.validate(config);
            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("jitter-factor")))
            );
        }
    }

    @Nested
    @DisplayName("Deserialization")
    class Deserialization {

        @Test
        @DisplayName("Should deserialize full YAML with attempts")
        void shouldDeserializeFullYaml() throws Exception {
            var yaml = "initial-backoff: \"PT1S\"\n"
                + "max-backoff: \"PT60S\"\n"
                + "backoff-multiplier: 2.0\n"
                + "reset-backoff-threshold: \"PT1H\"\n"
                + "jitter-factor: 0.1\n"
                + "attempts: 5\n";

            var config = mapper.readValue(yaml, ExponentialDelayRestartProperties.class);

            assertAll(
                () -> assertEquals(Duration.ofSeconds(1), config.initialBackoff().orElseThrow()),
                () -> assertEquals(Duration.ofSeconds(60), config.maxBackoff().orElseThrow()),
                () -> assertEquals(2.0, config.backoffMultiplier().orElseThrow()),
                () -> assertEquals(Duration.ofHours(1), config.resetBackoffThreshold().orElseThrow()),
                () -> assertEquals(0.1, config.jitterFactor().orElseThrow()),
                () -> assertEquals(5, config.attempts().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should deserialize minimal YAML with attempts omitted")
        void shouldDeserializeMinimalYaml() throws Exception {
            var yaml = "initial-backoff: \"PT0.5S\"\n"
                + "max-backoff: \"PT10S\"\n";

            var config = mapper.readValue(yaml, ExponentialDelayRestartProperties.class);

            assertAll(
                () -> assertEquals(Duration.ofMillis(500), config.initialBackoff().orElseThrow()),
                () -> assertEquals(Duration.ofSeconds(10), config.maxBackoff().orElseThrow()),
                () -> assertTrue(config.backoffMultiplier().isEmpty()),
                () -> assertTrue(config.resetBackoffThreshold().isEmpty()),
                () -> assertTrue(config.jitterFactor().isEmpty()),
                () -> assertTrue(config.attempts().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Should verify reflexive, symmetric equality and hashCode consistency")
        void shouldVerifyEquality() {
            var config1 = new ExponentialDelayRestartProperties(
                Duration.ofSeconds(1),
                Duration.ofMinutes(1),
                2.0,
                Duration.ofHours(1),
                0.1,
                5
            );
            var config2 = new ExponentialDelayRestartProperties(
                Duration.ofSeconds(1),
                Duration.ofMinutes(1),
                2.0,
                Duration.ofHours(1),
                0.1,
                5
            );

            assertAll(
                () -> assertEquals(config1, config1),
                () -> assertEquals(config1, config2),
                () -> assertEquals(config2, config1),
                () -> assertEquals(config1.hashCode(), config2.hashCode())
            );
        }

        @Test
        @DisplayName("Should verify inequality when any single field differs")
        void shouldVerifyInequality() {
            var base = new ExponentialDelayRestartProperties(
                Duration.ofSeconds(1),
                Duration.ofMinutes(1),
                2.0,
                Duration.ofHours(1),
                0.1,
                5
            );

            assertAll(
                () -> assertNotEquals(base, null),
                () -> assertNotEquals(base, "other-type"),
                () -> assertNotEquals(base, new ExponentialDelayRestartProperties(Duration.ofSeconds(2), Duration.ofMinutes(1), 2.0, Duration.ofHours(1), 0.1, 5)),
                () -> assertNotEquals(base, new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofMinutes(2), 2.0, Duration.ofHours(1), 0.1, 5)),
                () -> assertNotEquals(base, new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofMinutes(1), 3.0, Duration.ofHours(1), 0.1, 5)),
                () -> assertNotEquals(base, new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofMinutes(1), 2.0, Duration.ofHours(2), 0.1, 5)),
                () -> assertNotEquals(base, new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofMinutes(1), 2.0, Duration.ofHours(1), 0.5, 5)),
                () -> assertNotEquals(base, new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofMinutes(1), 2.0, Duration.ofHours(1), 0.1, 10))
            );
        }

        @Test
        @DisplayName("Should verify toString representation contains all field names and values")
        void shouldVerifyToString() {
            var config = new ExponentialDelayRestartProperties(
                Duration.ofSeconds(1),
                Duration.ofMinutes(1),
                2.0,
                Duration.ofHours(1),
                0.1,
                5
            );
            var str = config.toString();

            assertAll(
                () -> assertTrue(str.contains("initialBackoff=PT1S")),
                () -> assertTrue(str.contains("maxBackoff=PT1M")),
                () -> assertTrue(str.contains("backoffMultiplier=2.0")),
                () -> assertTrue(str.contains("resetBackoffThreshold=PT1H")),
                () -> assertTrue(str.contains("jitterFactor=0.1")),
                () -> assertTrue(str.contains("attempts=5"))
            );
        }
    }
}
