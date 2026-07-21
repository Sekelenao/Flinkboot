package io.github.sekelenao.flinkboot.core.api.configuration.checkpointing;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CheckpointingConfiguration Tests")
class CheckpointingConfigurationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Nested
    @DisplayName("Getters Tests")
    class GettersTests {

        @Test
        @DisplayName("Should return empty optionals when all parameters are null")
        void shouldReturnEmptyOptionalsWhenNull() {
            var config = new CheckpointingConfiguration(
                null, null, null, null, null, null, null, null, null, null
            );

            assertAll(
                () -> assertTrue(config.enabled().isEmpty()),
                () -> assertTrue(config.intervalMs().isEmpty()),
                () -> assertTrue(config.mode().isEmpty()),
                () -> assertTrue(config.timeoutMs().isEmpty()),
                () -> assertTrue(config.minPauseBetweenCheckpointsMs().isEmpty()),
                () -> assertTrue(config.maxConcurrentCheckpoints().isEmpty()),
                () -> assertTrue(config.externalizedCheckpointCleanup().isEmpty()),
                () -> assertTrue(config.unalignedCheckpoints().isEmpty()),
                () -> assertTrue(config.alignedCheckpointTimeoutMs().isEmpty()),
                () -> assertTrue(config.storageUri().isEmpty())
            );
        }

        @Test
        @DisplayName("Should return populated optionals when parameters are set")
        void shouldReturnPopulatedOptionals() {
            var config = new CheckpointingConfiguration(
                true,
                10000L,
                CheckpointingMode.EXACTLY_ONCE,
                60000L,
                5000L,
                2,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION,
                true,
                1000L,
                "file:///tmp/checkpoints"
            );

            assertAll(
                () -> assertEquals(true, config.enabled().orElseThrow()),
                () -> assertEquals(10000L, config.intervalMs().orElseThrow()),
                () -> assertEquals(CheckpointingMode.EXACTLY_ONCE, config.mode().orElseThrow()),
                () -> assertEquals(60000L, config.timeoutMs().orElseThrow()),
                () -> assertEquals(5000L, config.minPauseBetweenCheckpointsMs().orElseThrow()),
                () -> assertEquals(2, config.maxConcurrentCheckpoints().orElseThrow()),
                () -> assertEquals(ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, config.externalizedCheckpointCleanup().orElseThrow()),
                () -> assertEquals(true, config.unalignedCheckpoints().orElseThrow()),
                () -> assertEquals(1000L, config.alignedCheckpointTimeoutMs().orElseThrow()),
                () -> assertEquals("file:///tmp/checkpoints", config.storageUri().orElseThrow())
            );
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation when parameters are valid or null")
        void shouldPassValidationWhenValid() {
            var config = new CheckpointingConfiguration(
                true, 1000L, CheckpointingMode.EXACTLY_ONCE, 5000L, 0L, 1,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, false, 0L, "s3://bucket"
            );
            var violations = validator.validate(config);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when intervalMs is negative or zero")
        void shouldFailValidationWhenIntervalIsInvalid() {
            var config = new CheckpointingConfiguration(
                true, -10L, CheckpointingMode.EXACTLY_ONCE, 5000L, 0L, 1,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, false, 0L, "s3://bucket"
            );
            var violations = validator.validate(config);
            assertFalse(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should verify equals and hashCode contracts")
        void shouldVerifyEqualsAndHashCode() {
            var config1 = new CheckpointingConfiguration(
                true, 1000L, CheckpointingMode.EXACTLY_ONCE, 5000L, 0L, 1,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, false, 0L, "s3://bucket"
            );
            var config2 = new CheckpointingConfiguration(
                true, 1000L, CheckpointingMode.EXACTLY_ONCE, 5000L, 0L, 1,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION, false, 0L, "s3://bucket"
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
                "\"interval-ms\": 5000," +
                "\"mode\": \"EXACTLY_ONCE\"," +
                "\"timeout-ms\": 30000," +
                "\"min-pause-between-checkpoints-ms\": 1000," +
                "\"max-concurrent-checkpoints\": 1," +
                "\"externalized-checkpoint-cleanup\": \"RETAIN_ON_CANCELLATION\"," +
                "\"unaligned-checkpoints\": true," +
                "\"aligned-checkpoint-timeout-ms\": 500," +
                "\"storage-uri\": \"s3://my-bucket/checkpoints\"" +
                "}";

            var mapper = new ObjectMapper();
            var config = mapper.readValue(json, CheckpointingConfiguration.class);

            assertNotNull(config);
            assertEquals(5000L, config.intervalMs().orElseThrow());
            assertEquals(CheckpointingMode.EXACTLY_ONCE, config.mode().orElseThrow());
            assertEquals("s3://my-bucket/checkpoints", config.storageUri().orElseThrow());
        }
    }
}
