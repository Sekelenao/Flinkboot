package io.github.sekelenao.flinkboot.core.api.properties.checkpointing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CheckpointingProperties Tests")
class CheckpointingPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should return empty optionals when all parameters are null")
        void shouldReturnEmptyOptionalsWhenNull() {
            var config = new CheckpointingProperties(
                null, null, null, null, null, null, null, null, null, null
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
                () -> assertTrue(config.storageUri().isEmpty())
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
                "file:///tmp/checkpoints"
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
                () -> assertEquals("file:///tmp/checkpoints", config.storageUri().orElseThrow())
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
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, false, Duration.ZERO, "s3://bucket"
            );
            var violations = validator.validate(config);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should pass validation when all duration fields are null")
        void shouldPassValidationWhenDurationsAreNull() {
            var config = new CheckpointingProperties(
                null, null, null, null, null, null, null, null, null, null
            );
            var violations = validator.validate(config);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when interval is zero or negative")
        void shouldFailValidationWhenIntervalZeroOrNegative() {
            var zeroInterval = new CheckpointingProperties(
                true, Duration.ZERO, CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, null
            );
            var negativeInterval = new CheckpointingProperties(
                true, Duration.ofSeconds(-1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ZERO, null
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
                null, false, Duration.ZERO, null
            );
            var negativeTimeout = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(-10), Duration.ZERO, 1,
                null, false, Duration.ZERO, null
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
                null, false, Duration.ZERO, null
            );

            assertEquals(1, validator.validate(negativeMinPause).size());
        }

        @Test
        @DisplayName("Should fail validation when alignedCheckpointTimeout is negative")
        void shouldFailValidationWhenAlignedCheckpointTimeoutNegative() {
            var negativeTimeout = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                null, false, Duration.ofSeconds(-5), null
            );

            assertEquals(1, validator.validate(negativeTimeout).size());
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
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, false, Duration.ZERO, "s3://bucket"
            );
            var config2 = new CheckpointingProperties(
                true, Duration.ofSeconds(1), CheckpointingMode.EXACTLY_ONCE, Duration.ofSeconds(5), Duration.ZERO, 1,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, false, Duration.ZERO, "s3://bucket"
            );

            assertAll(
                () -> assertEquals(config1, config2),
                () -> assertEquals(config1.hashCode(), config2.hashCode())
            );
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
                "\"storage-uri\": \"s3://my-bucket/checkpoints\"" +
                "}";

            var mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            var config = mapper.readValue(json, CheckpointingProperties.class);

            assertNotNull(config);
            assertEquals(Duration.ofSeconds(5), config.interval().orElseThrow());
            assertEquals(CheckpointingMode.EXACTLY_ONCE, config.mode().orElseThrow());
            assertEquals("s3://my-bucket/checkpoints", config.storageUri().orElseThrow());
        }
    }
}

