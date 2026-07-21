package io.github.sekelenao.flinkboot.core.api.configuration.restart;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidRestartStrategyConfigurationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RestartStrategyConfiguration Tests")
class RestartStrategyConfigurationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should return empty optionals when all sub-configurations are null")
        void shouldReturnEmptyOptionals() {
            var config = new RestartStrategyConfiguration(null, null, null, null);

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
            var fixed = new FixedDelayRestartConfiguration(null, null);
            assertAll(
                () -> assertTrue(fixed.attempts().isEmpty()),
                () -> assertTrue(fixed.delayMs().isEmpty())
            );
        }

        @Test
        @DisplayName("Should return empty optionals on FailureRate sub-config getters when null")
        void shouldReturnEmptyOptionalsOnFailureRate() {
            var failure = new FailureRateRestartConfiguration(null, null, null);
            assertAll(
                () -> assertTrue(failure.maxFailuresPerInterval().isEmpty()),
                () -> assertTrue(failure.failureIntervalMs().isEmpty()),
                () -> assertTrue(failure.delayMs().isEmpty())
            );
        }

        @Test
        @DisplayName("Should return empty optionals on ExponentialDelay sub-config getters when null")
        void shouldReturnEmptyOptionalsOnExponentialDelay() {
            var expo = new ExponentialDelayRestartConfiguration(null, null, null, null, null);
            assertAll(
                () -> assertTrue(expo.initialBackoffMs().isEmpty()),
                () -> assertTrue(expo.maxBackoffMs().isEmpty()),
                () -> assertTrue(expo.backoffMultiplier().isEmpty()),
                () -> assertTrue(expo.resetBackoffThresholdMs().isEmpty()),
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
            var fixed = new FixedDelayRestartConfiguration(3, 5000L);
            var config = new RestartStrategyConfiguration(RestartStrategyType.FIXED_DELAY, fixed, null, null);

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
            var failure = new FailureRateRestartConfiguration(3, 60000L, 1000L);
            var config = new RestartStrategyConfiguration(RestartStrategyType.FAILURE_RATE, null, failure, null);

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
            var expo = new ExponentialDelayRestartConfiguration(1000L, 60000L, 2.0, 3600000L, 0.1);
            var config = new RestartStrategyConfiguration(RestartStrategyType.EXPONENTIAL_DELAY, null, null, expo);

            var violations = validator.validate(config);
            assertAll(
                () -> assertTrue(violations.isEmpty()),
                () -> assertEquals(RestartStrategyType.EXPONENTIAL_DELAY, config.type().orElseThrow()),
                () -> assertEquals(expo, config.exponentialDelay().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should throw Exception when sub-config provided for NO_RESTART")
        void shouldThrowExceptionWhenSubConfigProvidedForNoRestart() {
            var fixed = new FixedDelayRestartConfiguration(3, 5000L);
            assertThrows(
                InvalidRestartStrategyConfigurationException.class,
                () -> new RestartStrategyConfiguration(RestartStrategyType.NO_RESTART, fixed, null, null)
            );
        }

        @Test
        @DisplayName("Should throw Exception when sub-config provided for FALLBACK or null type")
        void shouldThrowExceptionWhenSubConfigProvidedForFallback() {
            var fixed = new FixedDelayRestartConfiguration(3, 5000L);
            assertAll(
                () -> assertThrows(InvalidRestartStrategyConfigurationException.class, () -> new RestartStrategyConfiguration(RestartStrategyType.FALLBACK, fixed, null, null)),
                () -> assertThrows(InvalidRestartStrategyConfigurationException.class, () -> new RestartStrategyConfiguration(null, fixed, null, null))
            );
        }

        @Test
        @DisplayName("Should throw Exception when incompatible failure-rate provided for FIXED_DELAY")
        void shouldThrowExceptionWhenIncompatibleSubConfigForFixedDelay() {
            var fixed = new FixedDelayRestartConfiguration(3, 5000L);
            var failure = new FailureRateRestartConfiguration(3, 60000L, 1000L);
            assertThrows(
                InvalidRestartStrategyConfigurationException.class,
                () -> new RestartStrategyConfiguration(RestartStrategyType.FIXED_DELAY, fixed, failure, null)
            );
        }

        @Test
        @DisplayName("Should throw Exception when incompatible fixed-delay provided for FAILURE_RATE")
        void shouldThrowExceptionWhenIncompatibleSubConfigForFailureRate() {
            var fixed = new FixedDelayRestartConfiguration(3, 5000L);
            var failure = new FailureRateRestartConfiguration(3, 60000L, 1000L);
            assertThrows(
                InvalidRestartStrategyConfigurationException.class,
                () -> new RestartStrategyConfiguration(RestartStrategyType.FAILURE_RATE, fixed, failure, null)
            );
        }

        @Test
        @DisplayName("Should throw Exception when incompatible fixed-delay provided for EXPONENTIAL_DELAY")
        void shouldThrowExceptionWhenIncompatibleSubConfigForExponentialDelay() {
            var fixed = new FixedDelayRestartConfiguration(3, 5000L);
            var expo = new ExponentialDelayRestartConfiguration(1000L, 60000L, 2.0, 3600000L, 0.1);
            assertThrows(
                InvalidRestartStrategyConfigurationException.class,
                () -> new RestartStrategyConfiguration(RestartStrategyType.EXPONENTIAL_DELAY, fixed, null, expo)
            );
        }

        @Test
        @DisplayName("Should throw Exception when maxBackoffMs < initialBackoffMs in exponential delay")
        void shouldThrowExceptionWhenMaxBackoffIsSmallerThanInitial() {
            var expo = new ExponentialDelayRestartConfiguration(10000L, 1000L, 2.0, 3600000L, 0.1);
            assertThrows(
                InvalidRestartStrategyConfigurationException.class,
                () -> new RestartStrategyConfiguration(RestartStrategyType.EXPONENTIAL_DELAY, null, null, expo)
            );
        }

        @Test
        @DisplayName("Should fail Bean Validation on negative attempts in FixedDelay")
        void shouldFailBeanValidationOnNegativeAttempts() {
            var fixed = new FixedDelayRestartConfiguration(-1, 5000L);
            var violations = validator.validate(fixed);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail Bean Validation on invalid multiplier or jitter in ExponentialDelay")
        void shouldFailBeanValidationOnInvalidExponentialParams() {
            var expoInvalidMultiplier = new ExponentialDelayRestartConfiguration(1000L, 60000L, 0.5, 3600000L, 0.1);
            var expoInvalidJitter = new ExponentialDelayRestartConfiguration(1000L, 60000L, 2.0, 3600000L, 1.5);
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
            var fixed1 = new FixedDelayRestartConfiguration(3, 5000L);
            var fixed2 = new FixedDelayRestartConfiguration(3, 5000L);
            var fixed3 = new FixedDelayRestartConfiguration(5, 5000L);

            var failure1 = new FailureRateRestartConfiguration(3, 60000L, 1000L);
            var failure2 = new FailureRateRestartConfiguration(3, 60000L, 1000L);

            var expo1 = new ExponentialDelayRestartConfiguration(1000L, 60000L, 2.0, 3600000L, 0.1);
            var expo2 = new ExponentialDelayRestartConfiguration(1000L, 60000L, 2.0, 3600000L, 0.1);

            var config1 = new RestartStrategyConfiguration(RestartStrategyType.FIXED_DELAY, fixed1, null, null);
            var config2 = new RestartStrategyConfiguration(RestartStrategyType.FIXED_DELAY, fixed2, null, null);

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
                "    \"delay-ms\": 10000\n" +
                "  }\n" +
                "}";

            RestartStrategyConfiguration config = mapper.readValue(json, RestartStrategyConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(RestartStrategyType.FIXED_DELAY, config.type().orElseThrow()),
                () -> assertEquals(5, config.fixedDelay().orElseThrow().attempts().orElseThrow()),
                () -> assertEquals(10000L, config.fixedDelay().orElseThrow().delayMs().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should deserialize failure-rate JSON configuration correctly")
        void shouldDeserializeFailureRateJson() throws Exception {
            String json = "{\n" +
                "  \"type\": \"FAILURE_RATE\",\n" +
                "  \"failure-rate\": {\n" +
                "    \"max-failures-per-interval\": 4,\n" +
                "    \"failure-interval-ms\": 120000,\n" +
                "    \"delay-ms\": 2000\n" +
                "  }\n" +
                "}";

            RestartStrategyConfiguration config = mapper.readValue(json, RestartStrategyConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(RestartStrategyType.FAILURE_RATE, config.type().orElseThrow()),
                () -> assertEquals(4, config.failureRate().orElseThrow().maxFailuresPerInterval().orElseThrow()),
                () -> assertEquals(120000L, config.failureRate().orElseThrow().failureIntervalMs().orElseThrow()),
                () -> assertEquals(2000L, config.failureRate().orElseThrow().delayMs().orElseThrow())
            );
        }

        @Test
        @DisplayName("Should deserialize exponential-delay JSON configuration correctly")
        void shouldDeserializeExponentialDelayJson() throws Exception {
            String json = "{\n" +
                "  \"type\": \"EXPONENTIAL_DELAY\",\n" +
                "  \"exponential-delay\": {\n" +
                "    \"initial-backoff-ms\": 1000,\n" +
                "    \"max-backoff-ms\": 30000,\n" +
                "    \"backoff-multiplier\": 1.5,\n" +
                "    \"reset-backoff-threshold-ms\": 1800000,\n" +
                "    \"jitter-factor\": 0.2\n" +
                "  }\n" +
                "}";

            RestartStrategyConfiguration config = mapper.readValue(json, RestartStrategyConfiguration.class);

            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(RestartStrategyType.EXPONENTIAL_DELAY, config.type().orElseThrow()),
                () -> assertEquals(1000L, config.exponentialDelay().orElseThrow().initialBackoffMs().orElseThrow()),
                () -> assertEquals(30000L, config.exponentialDelay().orElseThrow().maxBackoffMs().orElseThrow()),
                () -> assertEquals(1.5, config.exponentialDelay().orElseThrow().backoffMultiplier().orElseThrow()),
                () -> assertEquals(1800000L, config.exponentialDelay().orElseThrow().resetBackoffThresholdMs().orElseThrow()),
                () -> assertEquals(0.2, config.exponentialDelay().orElseThrow().jitterFactor().orElseThrow())
            );
        }
    }
}
