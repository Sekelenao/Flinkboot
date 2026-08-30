package io.github.sekelenao.flinkboot.core.api.properties.restart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidRestartStrategyPropertiesException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RestartStrategyProperties Tests")
class RestartStrategyPropertiesTest {

    private static Validator validator;
    private static ObjectMapper mapper;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should return empty optionals when all sub-configurations are null")
        void shouldReturnEmptyOptionals() {
            var config = new RestartStrategyProperties(null, null, null, null);

            assertAll(
                () -> assertTrue(config.type().isEmpty()),
                () -> assertTrue(config.fixedDelay().isEmpty()),
                () -> assertTrue(config.failureRate().isEmpty()),
                () -> assertTrue(config.exponentialDelay().isEmpty())
            );
        }

        @Test
        @DisplayName("Should return empty optionals on FixedDelay sub-config getters when null")
        void shouldReturnEmptyOptionalsOnFixedDelay() {
            var fixed = new FixedDelayRestartProperties(null, null);
            assertAll(
                () -> assertTrue(fixed.attempts().isEmpty()),
                () -> assertTrue(fixed.delay().isEmpty())
            );
        }

        @Test
        @DisplayName("Should return empty optionals on FailureRate sub-config getters when null")
        void shouldReturnEmptyOptionalsOnFailureRate() {
            var failure = new FailureRateRestartProperties(null, null, null);
            assertAll(
                () -> assertTrue(failure.maxFailuresPerInterval().isEmpty()),
                () -> assertTrue(failure.failureInterval().isEmpty()),
                () -> assertTrue(failure.delay().isEmpty())
            );
        }

        @Test
        @DisplayName("Should return empty optionals on ExponentialDelay sub-config getters when null")
        void shouldReturnEmptyOptionalsOnExponentialDelay() {
            var expo = new ExponentialDelayRestartProperties(null, null, null, null, null);
            assertAll(
                () -> assertTrue(expo.initialBackoff().isEmpty()),
                () -> assertTrue(expo.maxBackoff().isEmpty()),
                () -> assertTrue(expo.backoffMultiplier().isEmpty()),
                () -> assertTrue(expo.resetBackoffThreshold().isEmpty()),
                () -> assertTrue(expo.jitterFactor().isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Validation & Fail-Fast Exception Tests")
    class ValidationAndFailFastTests {

        @Test
        @DisplayName("Should pass validation with valid fixed delay strategy")
        void shouldPassWithValidFixedDelay() {
            var fixed = new FixedDelayRestartProperties(3, Duration.ofSeconds(5));
            var config = new RestartStrategyProperties(RestartStrategyType.FIXED_DELAY, fixed, null, null);

            var violations = validator.validate(config);
            assertAll(
                () -> assertTrue(violations.isEmpty()),
                () -> assertEquals(RestartStrategyType.FIXED_DELAY, config.type().orElseThrow()),
                () -> assertEquals(fixed, config.fixedDelay().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should pass validation with valid failure rate strategy")
        void shouldPassWithValidFailureRate() {
            var failure = new FailureRateRestartProperties(3, Duration.ofMinutes(1), Duration.ofSeconds(1));
            var config = new RestartStrategyProperties(RestartStrategyType.FAILURE_RATE, null, failure, null);

            var violations = validator.validate(config);
            assertAll(
                () -> assertTrue(violations.isEmpty()),
                () -> assertEquals(RestartStrategyType.FAILURE_RATE, config.type().orElseThrow()),
                () -> assertEquals(failure, config.failureRate().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should pass validation with valid exponential delay strategy")
        void shouldPassWithValidExponentialDelay() {
            var expo = new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofMinutes(1), 2.0, Duration.ofHours(1), 0.1);
            var config = new RestartStrategyProperties(RestartStrategyType.EXPONENTIAL_DELAY, null, null, expo);

            var violations = validator.validate(config);
            assertAll(
                () -> assertTrue(violations.isEmpty()),
                () -> assertEquals(RestartStrategyType.EXPONENTIAL_DELAY, config.type().orElseThrow()),
                () -> assertEquals(expo, config.exponentialDelay().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should pass validation when strategy sub-configurations have null durations")
        void shouldPassValidationWhenDurationsAreNull() {
            var fixed = new FixedDelayRestartProperties(null, null);
            var failure = new FailureRateRestartProperties(null, null, null);
            var expo = new ExponentialDelayRestartProperties(null, null, null, null, null);
            assertAll(
                () -> assertTrue(validator.validate(fixed).isEmpty()),
                () -> assertTrue(validator.validate(failure).isEmpty()),
                () -> assertTrue(validator.validate(expo).isEmpty())
            );
        }

        @Test
        @DisplayName("Should throw Exception when sub-config provided for NO_RESTART")
        void shouldThrowExceptionWhenSubConfigProvidedForNoRestart() {
            var fixed = new FixedDelayRestartProperties(3, Duration.ofSeconds(5));
            assertThrows(
                InvalidRestartStrategyPropertiesException.class,
                () -> new RestartStrategyProperties(RestartStrategyType.NO_RESTART, fixed, null, null)
            );
        }

        @Test
        @DisplayName("Should throw Exception when sub-config provided for FALLBACK or null type")
        void shouldThrowExceptionWhenSubConfigProvidedForFallback() {
            var fixed = new FixedDelayRestartProperties(3, Duration.ofSeconds(5));
            assertAll(
                () -> assertThrows(InvalidRestartStrategyPropertiesException.class, () -> new RestartStrategyProperties(RestartStrategyType.FALLBACK, fixed, null, null)),
                () -> assertThrows(InvalidRestartStrategyPropertiesException.class, () -> new RestartStrategyProperties(null, fixed, null, null))
            );
        }

        @Test
        @DisplayName("Should throw Exception when incompatible failure-rate provided for FIXED_DELAY")
        void shouldThrowExceptionWhenIncompatibleSubConfigForFixedDelay() {
            var fixed = new FixedDelayRestartProperties(3, Duration.ofSeconds(5));
            var failure = new FailureRateRestartProperties(3, Duration.ofMinutes(1), Duration.ofSeconds(1));
            assertThrows(
                InvalidRestartStrategyPropertiesException.class,
                () -> new RestartStrategyProperties(RestartStrategyType.FIXED_DELAY, fixed, failure, null)
            );
        }

        @Test
        @DisplayName("Should throw Exception when incompatible fixed-delay provided for FAILURE_RATE")
        void shouldThrowExceptionWhenIncompatibleSubConfigForFailureRate() {
            var fixed = new FixedDelayRestartProperties(3, Duration.ofSeconds(5));
            var failure = new FailureRateRestartProperties(3, Duration.ofMinutes(1), Duration.ofSeconds(1));
            assertThrows(
                InvalidRestartStrategyPropertiesException.class,
                () -> new RestartStrategyProperties(RestartStrategyType.FAILURE_RATE, fixed, failure, null)
            );
        }

        @Test
        @DisplayName("Should throw Exception when incompatible fixed-delay provided for EXPONENTIAL_DELAY")
        void shouldThrowExceptionWhenIncompatibleSubConfigForExponentialDelay() {
            var fixed = new FixedDelayRestartProperties(3, Duration.ofSeconds(5));
            var expo = new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofMinutes(1), 2.0, Duration.ofHours(1), 0.1);
            assertThrows(
                InvalidRestartStrategyPropertiesException.class,
                () -> new RestartStrategyProperties(RestartStrategyType.EXPONENTIAL_DELAY, fixed, null, expo)
            );
        }

        @Test
        @DisplayName("Should throw Exception when maxBackoff < initialBackoff in exponential delay")
        void shouldThrowExceptionWhenMaxBackoffIsSmallerThanInitial() {
            var expo = new ExponentialDelayRestartProperties(Duration.ofSeconds(10), Duration.ofSeconds(1), 2.0, Duration.ofHours(1), 0.1);
            assertThrows(
                InvalidRestartStrategyPropertiesException.class,
                () -> new RestartStrategyProperties(RestartStrategyType.EXPONENTIAL_DELAY, null, null, expo)
            );
        }

        @Test
        @DisplayName("Should fail Bean Validation on negative attempts or negative delay in FixedDelay")
        void shouldFailBeanValidationOnNegativeAttempts() {
            var fixedNegativeAttempts = new FixedDelayRestartProperties(-1, Duration.ofSeconds(5));
            var fixedNegativeDelay = new FixedDelayRestartProperties(3, Duration.ofSeconds(-1));
            assertAll(
                () -> assertFalse(validator.validate(fixedNegativeAttempts).isEmpty()),
                () -> assertFalse(validator.validate(fixedNegativeDelay).isEmpty())
            );
        }

        @Test
        @DisplayName("Should fail Bean Validation on zero or negative durations in FailureRate")
        void shouldFailBeanValidationOnInvalidFailureRateDurations() {
            var zeroInterval = new FailureRateRestartProperties(3, Duration.ZERO, Duration.ofSeconds(1));
            var negativeInterval = new FailureRateRestartProperties(3, Duration.ofSeconds(-1), Duration.ofSeconds(1));
            var negativeDelay = new FailureRateRestartProperties(3, Duration.ofSeconds(5), Duration.ofSeconds(-1));
            assertAll(
                () -> assertFalse(validator.validate(zeroInterval).isEmpty()),
                () -> assertFalse(validator.validate(negativeInterval).isEmpty()),
                () -> assertFalse(validator.validate(negativeDelay).isEmpty())
            );
        }

        @Test
        @DisplayName("Should fail Bean Validation on zero or negative durations in ExponentialDelay")
        void shouldFailBeanValidationOnInvalidExponentialDurations() {
            var zeroInitial = new ExponentialDelayRestartProperties(Duration.ZERO, Duration.ofMinutes(1), 2.0, Duration.ofHours(1), 0.1);
            var zeroMax = new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ZERO, 2.0, Duration.ofHours(1), 0.1);
            var zeroReset = new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofMinutes(1), 2.0, Duration.ZERO, 0.1);
            var negativeInitial = new ExponentialDelayRestartProperties(Duration.ofSeconds(-1), Duration.ofMinutes(1), 2.0, Duration.ofHours(1), 0.1);
            assertAll(
                () -> assertFalse(validator.validate(zeroInitial).isEmpty()),
                () -> assertFalse(validator.validate(zeroMax).isEmpty()),
                () -> assertFalse(validator.validate(zeroReset).isEmpty()),
                () -> assertFalse(validator.validate(negativeInitial).isEmpty())
            );
        }

        @Test
        @DisplayName("Should fail Bean Validation on invalid multiplier or jitter in ExponentialDelay")
        void shouldFailBeanValidationOnInvalidExponentialParams() {
            var expoInvalidMultiplier = new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofMinutes(1), 0.5, Duration.ofHours(1), 0.1);
            var expoInvalidJitter = new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofMinutes(1), 2.0, Duration.ofHours(1), 1.5);
            assertAll(
                () -> assertFalse(validator.validate(expoInvalidMultiplier).isEmpty()),
                () -> assertFalse(validator.validate(expoInvalidJitter).isEmpty())
            );
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should verify equals and hashCode contract across strategy configurations")
        void shouldVerifyEqualsAndHashCode() {
            var fixed1 = new FixedDelayRestartProperties(3, Duration.ofSeconds(5));
            var fixed2 = new FixedDelayRestartProperties(3, Duration.ofSeconds(5));
            var fixed3 = new FixedDelayRestartProperties(5, Duration.ofSeconds(5));

            var failure1 = new FailureRateRestartProperties(3, Duration.ofMinutes(1), Duration.ofSeconds(1));
            var failure2 = new FailureRateRestartProperties(3, Duration.ofMinutes(1), Duration.ofSeconds(1));

            var expo1 = new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofMinutes(1), 2.0, Duration.ofHours(1), 0.1);
            var expo2 = new ExponentialDelayRestartProperties(Duration.ofSeconds(1), Duration.ofMinutes(1), 2.0, Duration.ofHours(1), 0.1);

            var config1 = new RestartStrategyProperties(RestartStrategyType.FIXED_DELAY, fixed1, null, null);
            var config2 = new RestartStrategyProperties(RestartStrategyType.FIXED_DELAY, fixed2, null, null);

            assertAll(
                () -> assertEquals(fixed1, fixed2),
                () -> assertEquals(fixed1.hashCode(), fixed2.hashCode()),
                () -> assertNotEquals(fixed1, fixed3),
                () -> assertEquals(failure1, failure2),
                () -> assertEquals(failure1.hashCode(), failure2.hashCode()),
                () -> assertEquals(expo1, expo2),
                () -> assertEquals(expo1.hashCode(), expo2.hashCode()),
                () -> assertEquals(config1, config2),
                () -> assertEquals(config1.hashCode(), config2.hashCode())
            );
        }
    }

    @Nested
    @DisplayName("Deserialization Tests")
    class DeserializationTests {

        @Test
        @DisplayName("Should deserialize fixed-delay JSON configuration correctly")
        void shouldDeserializeFixedDelayJson() throws Exception {
            String json = "{\n" +
                "  \"type\": \"FIXED_DELAY\",\n" +
                "  \"fixed-delay\": {\n" +
                "    \"attempts\": 5,\n" +
                "    \"delay\": \"PT10S\"\n" +
                "  }\n" +
                "}";

            RestartStrategyProperties config = mapper.readValue(json, RestartStrategyProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(RestartStrategyType.FIXED_DELAY, config.type().orElseThrow()),
                () -> assertEquals(5, config.fixedDelay().orElseThrow().attempts().orElseThrow()),
                () -> assertEquals(Duration.ofSeconds(10), config.fixedDelay().orElseThrow().delay().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should deserialize failure-rate JSON configuration correctly")
        void shouldDeserializeFailureRateJson() throws Exception {
            String json = "{\n" +
                "  \"type\": \"FAILURE_RATE\",\n" +
                "  \"failure-rate\": {\n" +
                "    \"max-failures-per-interval\": 4,\n" +
                "    \"failure-interval\": \"PT2M\",\n" +
                "    \"delay\": \"PT2S\"\n" +
                "  }\n" +
                "}";

            RestartStrategyProperties config = mapper.readValue(json, RestartStrategyProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(RestartStrategyType.FAILURE_RATE, config.type().orElseThrow()),
                () -> assertEquals(4, config.failureRate().orElseThrow().maxFailuresPerInterval().orElseThrow()),
                () -> assertEquals(Duration.ofMinutes(2), config.failureRate().orElseThrow().failureInterval().orElseThrow()),
                () -> assertEquals(Duration.ofSeconds(2), config.failureRate().orElseThrow().delay().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should deserialize exponential-delay JSON configuration correctly")
        void shouldDeserializeExponentialDelayJson() throws Exception {
            String json = "{\n" +
                "  \"type\": \"EXPONENTIAL_DELAY\",\n" +
                "  \"exponential-delay\": {\n" +
                "    \"initial-backoff\": \"PT1S\",\n" +
                "    \"max-backoff\": \"PT30S\",\n" +
                "    \"backoff-multiplier\": 1.5,\n" +
                "    \"reset-backoff-threshold\": \"PT30M\",\n" +
                "    \"jitter-factor\": 0.2\n" +
                "  }\n" +
                "}";

            RestartStrategyProperties config = mapper.readValue(json, RestartStrategyProperties.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(RestartStrategyType.EXPONENTIAL_DELAY, config.type().orElseThrow()),
                () -> assertEquals(Duration.ofSeconds(1), config.exponentialDelay().orElseThrow().initialBackoff().orElseThrow()),
                () -> assertEquals(Duration.ofSeconds(30), config.exponentialDelay().orElseThrow().maxBackoff().orElseThrow()),
                () -> assertEquals(1.5, config.exponentialDelay().orElseThrow().backoffMultiplier().orElseThrow()),
                () -> assertEquals(Duration.ofMinutes(30), config.exponentialDelay().orElseThrow().resetBackoffThreshold().orElseThrow()),
                () -> assertEquals(0.2, config.exponentialDelay().orElseThrow().jitterFactor().orElseThrow())
            );
        }
    }
}

