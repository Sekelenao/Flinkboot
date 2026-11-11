package com.flinkboot.core.api.properties.checkpointing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flinkboot.core.internal.validation.Validators;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CheckpointingProperties Tests")
class CheckpointingPropertiesTest {

    private static final Validator validator;

    static {
        try (var factory = Validators.factory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should return empty optionals when all parameters are null")
        void shouldReturnEmptyOptionalsWhenNull() {
            var config = new CheckpointingProperties(
                null, null, null, null, null, null, null, null, null, null, null, null
            );

            assertAll(
                () -> assertTrue(config.enabled().isEmpty()),
                () -> assertTrue(config.interval().isEmpty()),
                () -> assertTrue(config.mode().isEmpty()),
                () -> assertTrue(config.timeout().isEmpty()),
                () -> assertTrue(config.minPauseBetweenCheckpoints().isEmpty()),
                () -> assertTrue(config.maxConcurrentCheckpoints().isEmpty()),
                () -> assertTrue(config.externalizedCheckpointCleanup().isEmpty()),
                () -> assertTrue(config.unalignedCheckpoints().isEmpty()),
                () -> assertTrue(config.alignedCheckpointTimeout().isEmpty()),
                () -> assertTrue(config.storage().isEmpty()),
                () -> assertTrue(config.storageUri().isEmpty()),
                () -> assertTrue(config.tolerableCheckpointFailures().isEmpty())
            );
        }

        @Test
        @DisplayName("Should return populated optionals when parameters are set")
        void shouldReturnPopulatedOptionals() {
            var config = new CheckpointingProperties(
                true,
                Duration.ofSeconds(10),
                CheckpointingMode.EXACTLY_ONCE,
                Duration.ofMinutes(1),
                Duration.ofSeconds(5),
                2,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION,
                true,
                Duration.ofSeconds(1),
                CheckpointStorageType.FILESYSTEM,
                "file:///tmp/checkpoints",
                3
            );

            assertAll(
                () -> assertEquals(true, config.enabled().orElseThrow()),
                () -> assertEquals(Duration.ofSeconds(10), config.interval().orElseThrow()),
                () -> assertEquals(CheckpointingMode.EXACTLY_ONCE, config.mode().orElseThrow()),
                () -> assertEquals(Duration.ofMinutes(1), config.timeout().orElseThrow()),
                () -> assertEquals(Duration.ofSeconds(5), config.minPauseBetweenCheckpoints().orElseThrow()),
                () -> assertEquals(2, config.maxConcurrentCheckpoints().orElseThrow()),
                () -> assertEquals(ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, config.externalizedCheckpointCleanup().orElseThrow()),
                () -> assertEquals(true, config.unalignedCheckpoints().orElseThrow()),
                () -> assertEquals(Duration.ofSeconds(1), config.alignedCheckpointTimeout().orElseThrow()),
                () -> assertEquals(CheckpointStorageType.FILESYSTEM, config.storage().orElseThrow()),
                () -> assertEquals("file:///tmp/checkpoints", config.storageUri().orElseThrow()),
                () -> assertEquals(3, config.tolerableCheckpointFailures().orElseThrow())
            );
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation when parameters are valid")
        void shouldPassValidationWhenValid() {
            var config = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, false, Duration.ZERO, CheckpointStorageType.FILESYSTEM, "s3://bucket", 0
            );
            var violations = validator.validate(config);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should pass validation when tolerableCheckpointFailures is positive or zero")
        void shouldPassValidationWhenTolerableCheckpointFailuresPositiveOrZero() {
            var configZero = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, null, null, 0
            );
            var configPositive = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, null, null, 5
            );

            assertAll(
                () -> assertTrue(validator.validate(configZero).isEmpty()),
                () -> assertTrue(validator.validate(configPositive).isEmpty())
            );
        }

        @Test
        @DisplayName("Should fail validation when tolerableCheckpointFailures is negative")
        void shouldFailValidationWhenTolerableCheckpointFailuresNegative() {
            var config = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, null, null, -1
            );
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("tolerable-checkpoint-failures")
                ), "Violation must target 'tolerable-checkpoint-failures'")
            );
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(booleans = {true})
        @DisplayName("Should fail validation when checkpointing is enabled or default without an interval")
        void shouldFailValidationWhenCheckpointingIsEnabledWithoutInterval(Boolean enabled) {
            var config = new CheckpointingProperties(
                enabled, null, null, null, null, null, null, null, null, null, null, null
            );

            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(
                    violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("interval")
                            && v.getMessageTemplate().equals("interval must be specified when checkpointing is enabled")
                    ),
                    "Expected violation on property 'interval'"
                )
            );
        }

        @Test
        @DisplayName("Should reject concurrent checkpoints when min pause is positive")
        void shouldFailWhenMinPauseIsPositiveAndMaxConcurrentExceedsOne() {
            var config = new CheckpointingProperties(
                true, Duration.ofSeconds(10), null, null, Duration.ofSeconds(5), 2,
                null, null, null, null, null, null
            );

            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("max-concurrent-checkpoints")
                        && v.getMessageTemplate().equals(
                            "max-concurrent-checkpoints must be 1 when min-pause-between-checkpoints is greater than 0"
                        )
                ), "Expected violation on property 'max-concurrent-checkpoints'")
            );
        }

        @Test
        @DisplayName("Should pass validation when checkpointing is disabled without an interval")
        void shouldPassValidationWhenCheckpointingIsDisabledWithoutInterval() {
            var config = new CheckpointingProperties(
                false, null, null, null, null, null, null, null, null, null, null, null
            );

            var violations = validator.validate(config);

            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when interval is zero or negative")
        void shouldFailValidationWhenIntervalZeroOrNegative() {
            var zeroInterval = new CheckpointingProperties(
                true, Duration.ZERO, CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, null, null, null
            );
            var negativeInterval = new CheckpointingProperties(
                true, Duration.ofSeconds(-1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, null, null, null
            );

            assertAll(
                () -> assertEquals(1, validator.validate(zeroInterval).size()),
                () -> assertEquals(1, validator.validate(negativeInterval).size())
            );
        }

        @Test
        @DisplayName("Should fail validation when timeout is zero or negative")
        void shouldFailValidationWhenTimeoutZeroOrNegative() {
            var zeroTimeout = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ZERO, Duration.ZERO, 1,
                null, false, Duration.ZERO, null, null, null
            );
            var negativeTimeout = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(-10), Duration.ZERO, 1,
                null, false, Duration.ZERO, null, null, null
            );

            assertAll(
                () -> assertEquals(1, validator.validate(zeroTimeout).size()),
                () -> assertEquals(1, validator.validate(negativeTimeout).size())
            );
        }

        @Test
        @DisplayName("Should fail validation when minPauseBetweenCheckpoints is negative")
        void shouldFailValidationWhenMinPauseNegative() {
            var negativeMinPause = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ofSeconds(-1), 1,
                null, false, Duration.ZERO, null, null, null
            );
            var violations = validator.validate(negativeMinPause);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("min-pause-between-checkpoints")
                ), "Violation must target 'min-pause-between-checkpoints'")
            );
        }

        @Test
        @DisplayName("Should fail validation when alignedCheckpointTimeout is negative")
        void shouldFailValidationWhenAlignedCheckpointTimeoutNegative() {
            var negativeTimeout = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ofSeconds(-5), null, null, null
            );
            var violations = validator.validate(negativeTimeout);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("aligned-checkpoint-timeout")
                ), "Violation must target 'aligned-checkpoint-timeout'")
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Should fail validation when storageUri is empty or blank")
        void shouldFailValidationWhenStorageUriBlank(String storageUri) {
            var config = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, null, storageUri, null
            );
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("storage-uri")
                        && v.getMessage().equals("must not be blank")
                ))
            );
        }

        @Test
        @DisplayName("Should pass validation when storageUri is null")
        void shouldPassValidationWhenStorageUriNull() {
            var config = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, null, null, null
            );
            var violations = validator.validate(config);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when storage is FILESYSTEM and storageUri is null")
        void shouldFailWhenStorageIsFileSystemAndStorageUriIsNull() {
            var config = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, CheckpointStorageType.FILESYSTEM, null, null
            );
            var violations = validator.validate(config);

            assertAll(
                () -> assertEquals(1, violations.size()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("storage-uri")
                        && v.getMessage().equals("storage-uri must be specified when checkpoint storage is FILESYSTEM")
                ), "Violation must target 'storage-uri' with expected message")
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Should fail validation when storage is FILESYSTEM and storageUri is blank")
        void shouldFailWhenStorageIsFileSystemAndStorageUriIsBlank(String blankUri) {
            var config = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, CheckpointStorageType.FILESYSTEM, blankUri, null
            );
            var violations = validator.validate(config);

            assertAll(
                () -> assertFalse(violations.isEmpty()),
                () -> assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("storage-uri")
                        && v.getMessage().equals("storage-uri must be specified when checkpoint storage is FILESYSTEM")
                ))
            );
        }

        @Test
        @DisplayName("Should pass validation when storage is JOBMANAGER and storageUri is null")
        void shouldPassWhenStorageIsJobManagerAndStorageUriIsNull() {
            var config = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, CheckpointStorageType.JOBMANAGER, null, null
            );
            var violations = validator.validate(config);
            assertTrue(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should verify equals and hashCode contracts")
        void shouldVerifyEqualsAndHashCode() {
            var config1 = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, false, Duration.ZERO, CheckpointStorageType.FILESYSTEM, "s3://bucket", 2
            );
            var config2 = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, false, Duration.ZERO, CheckpointStorageType.FILESYSTEM, "s3://bucket", 2
            );
            var diffStorage = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, false, Duration.ZERO, CheckpointStorageType.JOBMANAGER, "s3://bucket", 2
            );
            var diffFailures = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, false, Duration.ZERO, CheckpointStorageType.FILESYSTEM, "s3://bucket", 5
            );

            assertAll(
                () -> assertEquals(config1, config2),
                () -> assertEquals(config1.hashCode(), config2.hashCode()),
                () -> assertNotEquals(config1, diffStorage),
                () -> assertNotEquals(config1, diffFailures)
            );
        }

        @Test
        @DisplayName("Should include storage in toString")
        void shouldIncludeStorageInToString() {
            var config = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, CheckpointStorageType.FILESYSTEM, "s3://bucket", 3
            );
            var str = config.toString();
            assertTrue(str.contains("storage=FILESYSTEM"));
        }

        @Test
        @DisplayName("Should include tolerableCheckpointFailures in toString")
        void shouldIncludeTolerableCheckpointFailuresInToString() {
            var config = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, null, null, 3
            );
            var str = config.toString();
            assertTrue(str.contains("tolerableCheckpointFailures=3"));
        }
    }

    @Nested
    @DisplayName("Deserialization Tests")
    class DeserializationTests {

        @Test
        @DisplayName("Should deserialize JSON/YAML correctly with kebab-case properties")
        void shouldDeserializeJson() throws Exception {
            String json = "{" +
                "\"enabled\": true," +
                "\"interval\": \"PT5S\"," +
                "\"mode\": \"EXACTLY_ONCE\"," +
                "\"timeout\": \"PT30S\"," +
                "\"min-pause-between-checkpoints\": \"PT1S\"," +
                "\"max-concurrent-checkpoints\": 1," +
                "\"externalized-checkpoint-cleanup\": \"RETAIN_ON_CANCELLATION\"," +
                "\"unaligned-checkpoints\": true," +
                "\"aligned-checkpoint-timeout\": \"PT0.5S\"," +
                "\"storage\": \"FILESYSTEM\"," +
                "\"storage-uri\": \"s3://my-bucket/checkpoints\"," +
                "\"tolerable-checkpoint-failures\": 3" +
                "}";

            var mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            var config = mapper.readValue(json, CheckpointingProperties.class);

            assertNotNull(config);
            assertEquals(Duration.ofSeconds(5), config.interval().orElseThrow());
            assertEquals(CheckpointingMode.EXACTLY_ONCE, config.mode().orElseThrow());
            assertEquals(CheckpointStorageType.FILESYSTEM, config.storage().orElseThrow());
            assertEquals("s3://my-bucket/checkpoints", config.storageUri().orElseThrow());
            assertEquals(3, config.tolerableCheckpointFailures().orElseThrow());
        }

        @Test
        @DisplayName("Should deserialize JOBMANAGER storage from JSON")
        void shouldDeserializeJobManagerStorage() throws Exception {
            String json = "{" +
                "\"storage\": \"JOBMANAGER\"" +
                "}";

            var mapper = new ObjectMapper();
            var config = mapper.readValue(json, CheckpointingProperties.class);

            assertNotNull(config);
            assertAll(
                () -> assertEquals(CheckpointStorageType.JOBMANAGER, config.storage().orElseThrow()),
                () -> assertTrue(config.tolerableCheckpointFailures().isEmpty())
            );
        }
    }
}
