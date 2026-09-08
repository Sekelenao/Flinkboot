package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingMode;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.ExternalizedCheckpointCleanupMode;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.ExternalizedCheckpointRetention;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;

import static org.apache.flink.core.execution.CheckpointingMode.AT_LEAST_ONCE;
import static org.apache.flink.core.execution.CheckpointingMode.EXACTLY_ONCE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CheckpointingCustomizer Tests")
class CheckpointingCustomizerTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw NullPointerException when Configuration is null")
        void shouldThrowNullPointerExceptionWhenConfigurationIsNull() {
            assertThrows(NullPointerException.class, () -> new CheckpointingCustomizer(null));
        }
    }

    @Nested
    @DisplayName("Configure")
    class Configure {

        @Test
        @DisplayName("Should throw NullPointerException when ExecutionEnvironmentProperties is null")
        void shouldThrowNullPointerExceptionWhenPropertiesAreNull() {
            var customizer = new CheckpointingCustomizer(new Configuration());
            assertThrows(NullPointerException.class, () -> customizer.configure(null));
        }

        @Test
        @DisplayName("Should do nothing when checkpointing configuration is empty")
        void shouldDoNothingWhenEmpty() {
            var flinkConfig = new Configuration();
            var customizer = new CheckpointingCustomizer(flinkConfig);
            var envProps = new ExecutionEnvironmentProperties(null, null, null, null, null, null, null);
            customizer.configure(envProps);
            assertAll(
                () -> assertNull(flinkConfig.get(CheckpointingOptions.CHECKPOINTING_INTERVAL)),
                () -> assertTrue(flinkConfig.toMap().isEmpty())
            );
        }

        @Test
        @DisplayName("Should do nothing when checkpointing configuration is present but all properties are null")
        void shouldDoNothingWhenCheckpointingPropertiesAreAllNull() {
            var flinkConfig = new Configuration();
            var customizer = new CheckpointingCustomizer(flinkConfig);
            var checkpointConfig = new CheckpointingProperties(
                null, null, null, null, null, null, null, null, null, null
            );
            var envProps = new ExecutionEnvironmentProperties(null, checkpointConfig, null, null, null, null, null);
            customizer.configure(envProps);
            assertTrue(flinkConfig.toMap().isEmpty(), "No configuration options should be set when properties are all null");
        }

        @Test
        @DisplayName("Should skip checkpointing configuration when explicitly disabled")
        void shouldSkipCheckpointingPropertiesWhenDisabled() {
            var flinkConfig = new Configuration();
            var customizer = new CheckpointingCustomizer(flinkConfig);
            var checkpointConfig = new CheckpointingProperties(
                false,
                Duration.ofMillis(10000L),
                CheckpointingMode.EXACTLY_ONCE,
                Duration.ofMillis(60000L),
                Duration.ofMillis(5000L),
                2,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION,
                true,
                Duration.ofMillis(1000L),
                "s3://bucket/checkpoints"
            );
            var envProps = new ExecutionEnvironmentProperties(null, checkpointConfig, null, null, null, null, null);

            customizer.configure(envProps);

            assertTrue(flinkConfig.toMap().isEmpty());
        }

        @Test
        @DisplayName("Should apply all checkpointing properties when fully configured and enabled")
        void shouldApplyCheckpointingProperties() {
            var flinkConfig = new Configuration();
            var customizer = new CheckpointingCustomizer(flinkConfig);
            var checkpointConfig = new CheckpointingProperties(
                true,
                Duration.ofMillis(10000L),
                CheckpointingMode.EXACTLY_ONCE,
                Duration.ofMillis(60000L),
                Duration.ofMillis(5000L),
                2,
                ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION,
                true,
                Duration.ofMillis(1000L),
                "s3://bucket/checkpoints"
            );
            var envProps = new ExecutionEnvironmentProperties(null, checkpointConfig, null, null, null, null, null);
            customizer.configure(envProps);
            assertAll(
                () -> assertEquals(Duration.ofMillis(10000L), flinkConfig.get(CheckpointingOptions.CHECKPOINTING_INTERVAL)),
                () -> assertEquals(EXACTLY_ONCE, flinkConfig.get(CheckpointingOptions.CHECKPOINTING_CONSISTENCY_MODE)),
                () -> assertEquals(Duration.ofMillis(60000L), flinkConfig.get(CheckpointingOptions.CHECKPOINTING_TIMEOUT)),
                () -> assertEquals(Duration.ofMillis(5000L), flinkConfig.get(CheckpointingOptions.MIN_PAUSE_BETWEEN_CHECKPOINTS)),
                () -> assertEquals(2, flinkConfig.get(CheckpointingOptions.MAX_CONCURRENT_CHECKPOINTS)),
                () -> assertEquals(ExternalizedCheckpointRetention.RETAIN_ON_CANCELLATION, flinkConfig.get(CheckpointingOptions.EXTERNALIZED_CHECKPOINT_RETENTION)),
                () -> assertTrue(flinkConfig.get(CheckpointingOptions.ENABLE_UNALIGNED)),
                () -> assertEquals(Duration.ofMillis(1000L), flinkConfig.get(CheckpointingOptions.ALIGNED_CHECKPOINT_TIMEOUT)),
                () -> assertEquals("s3://bucket/checkpoints", flinkConfig.get(CheckpointingOptions.CHECKPOINTS_DIRECTORY)),
                () -> assertEquals(9, flinkConfig.toMap().size())
            );
        }

        @Test
        @DisplayName("Should apply checkpointing configuration when enabled is not specified")
        void shouldApplyCheckpointingPropertiesWhenEnabledIsNotSpecified() {
            var flinkConfig = new Configuration();
            var customizer = new CheckpointingCustomizer(flinkConfig);
            var checkpointConfig = new CheckpointingProperties(
                null,
                Duration.ofMillis(10000L),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );
            var envProps = new ExecutionEnvironmentProperties(null, checkpointConfig, null, null, null, null, null);
            customizer.configure(envProps);
            assertAll(
                () -> assertEquals(Duration.ofMillis(10000L), flinkConfig.get(CheckpointingOptions.CHECKPOINTING_INTERVAL)),
                () -> assertEquals(1, flinkConfig.toMap().size(), "Only the specified interval should be added")
            );
        }

        @ParameterizedTest
        @EnumSource(CheckpointingMode.class)
        @DisplayName("Should map CheckpointingMode to Flink consistency mode")
        void shouldMapCheckpointingMode(CheckpointingMode mode) {
            var flinkConfig = new Configuration();
            var customizer = new CheckpointingCustomizer(flinkConfig);
            var checkpointConfig = new CheckpointingProperties(
                null, null, mode, null, null, null, null, null, null, null
            );
            var envProps = new ExecutionEnvironmentProperties(null, checkpointConfig, null, null, null, null, null);
            customizer.configure(envProps);
            var expectedFlinkMode = mode == CheckpointingMode.EXACTLY_ONCE ? EXACTLY_ONCE : AT_LEAST_ONCE;
            assertAll(
                () -> assertEquals(expectedFlinkMode, flinkConfig.get(CheckpointingOptions.CHECKPOINTING_CONSISTENCY_MODE)),
                () -> assertEquals(1, flinkConfig.toMap().size())
            );
        }

        @ParameterizedTest(name = "Cleanup mode {0} should map to retention {1}")
        @CsvSource({
            "RETAIN_ON_CANCELLATION, RETAIN_ON_CANCELLATION",
            "DELETE_ON_CANCELLATION, DELETE_ON_CANCELLATION",
            "NO_EXTERNALIZED_CHECKPOINTS, NO_EXTERNALIZED_CHECKPOINTS"
        })
        @DisplayName("Should map ExternalizedCheckpointCleanupMode to Flink retention mode")
        void shouldMapExternalizedCheckpointCleanupMode(
            ExternalizedCheckpointCleanupMode cleanupMode,
            ExternalizedCheckpointRetention expectedRetention
        ) {
            var flinkConfig = new Configuration();
            var customizer = new CheckpointingCustomizer(flinkConfig);
            var checkpointConfig = new CheckpointingProperties(
                null, null, null, null, null, null, cleanupMode, null, null, null
            );
            var envProps = new ExecutionEnvironmentProperties(null, checkpointConfig, null, null, null, null, null);
            customizer.configure(envProps);
            assertAll(
                () -> assertEquals(expectedRetention, flinkConfig.get(CheckpointingOptions.EXTERNALIZED_CHECKPOINT_RETENTION)),
                () -> assertEquals(1, flinkConfig.toMap().size())
            );
        }

        @Test
        @DisplayName("Should configure unaligned checkpoints to true in isolation")
        void shouldConfigureUnalignedCheckpointsToTrue() {
            var flinkConfig = new Configuration();
            var customizer = new CheckpointingCustomizer(flinkConfig);
            var checkpointConfig = new CheckpointingProperties(
                null, null, null, null, null, null, null, true, null, null
            );
            var envProps = new ExecutionEnvironmentProperties(null, checkpointConfig, null, null, null, null, null);
            customizer.configure(envProps);
            assertAll(
                () -> assertTrue(flinkConfig.get(CheckpointingOptions.ENABLE_UNALIGNED)),
                () -> assertEquals(1, flinkConfig.toMap().size())
            );
        }

        @Test
        @DisplayName("Should configure unaligned checkpoints to false")
        void shouldConfigureUnalignedCheckpointsToFalse() {
            var flinkConfig = new Configuration();
            var customizer = new CheckpointingCustomizer(flinkConfig);
            var checkpointConfig = new CheckpointingProperties(
                null, null, null, null, null, null, null, false, null, null
            );
            var envProps = new ExecutionEnvironmentProperties(null, checkpointConfig, null, null, null, null, null);
            customizer.configure(envProps);
            assertAll(
                () -> assertFalse(flinkConfig.get(CheckpointingOptions.ENABLE_UNALIGNED)),
                () -> assertEquals(1, flinkConfig.toMap().size())
            );
        }

        @Test
        @DisplayName("Should preserve existing configuration entries when applying checkpointing")
        void shouldPreserveExistingConfigurationEntries() {
            var flinkConfig = new Configuration();
            flinkConfig.set(CoreOptions.DEFAULT_PARALLELISM, 8);
            var customizer = new CheckpointingCustomizer(flinkConfig);
            var checkpointConfig = new CheckpointingProperties(
                true, Duration.ofSeconds(10), null, null, null, null, null, null, null, null
            );
            var envProps = new ExecutionEnvironmentProperties(null, checkpointConfig, null, null, null, null, null);
            customizer.configure(envProps);
            assertAll(
                () -> assertEquals(8, flinkConfig.get(CoreOptions.DEFAULT_PARALLELISM)),
                () -> assertEquals(Duration.ofSeconds(10), flinkConfig.get(CheckpointingOptions.CHECKPOINTING_INTERVAL)),
                () -> assertEquals(2, flinkConfig.toMap().size())
            );
        }
    }
}

