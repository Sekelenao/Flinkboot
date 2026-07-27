package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingMode;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.ExternalizedCheckpointCleanupMode;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ExternalizedCheckpointRetention;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CheckpointingCustomizer Tests")
class CheckpointingCustomizerTest {

    @Test
    @DisplayName("Should apply checkpointing configuration onto Flink configuration")
    void shouldApplyCheckpointingProperties() {
        Configuration flinkConfig = new Configuration();
        var customizer = new CheckpointingCustomizer(flinkConfig);

        var checkpointConfig = new CheckpointingProperties(
            true,
            10000L,
            CheckpointingMode.EXACTLY_ONCE,
            60000L,
            5000L,
            2,
            ExternalizedCheckpointCleanupMode.RETAIN_ON_CANCELLATION,
            true,
            1000L,
            "s3://bucket/checkpoints"
        );
        var envProps = new ExecutionEnvironmentProperties(null, checkpointConfig, null, null, null, null, null);

        customizer.configure(envProps);

        assertAll(
            () -> assertEquals(Duration.ofMillis(10000L), flinkConfig.get(CheckpointingOptions.CHECKPOINTING_INTERVAL)),
            () -> assertEquals(org.apache.flink.core.execution.CheckpointingMode.EXACTLY_ONCE, flinkConfig.get(CheckpointingOptions.CHECKPOINTING_CONSISTENCY_MODE)),
            () -> assertEquals(Duration.ofMillis(60000L), flinkConfig.get(CheckpointingOptions.CHECKPOINTING_TIMEOUT)),
            () -> assertEquals(Duration.ofMillis(5000L), flinkConfig.get(CheckpointingOptions.MIN_PAUSE_BETWEEN_CHECKPOINTS)),
            () -> assertEquals(2, flinkConfig.get(CheckpointingOptions.MAX_CONCURRENT_CHECKPOINTS)),
            () -> assertEquals(ExternalizedCheckpointRetention.RETAIN_ON_CANCELLATION, flinkConfig.get(CheckpointingOptions.EXTERNALIZED_CHECKPOINT_RETENTION)),
            () -> assertTrue(flinkConfig.get(CheckpointingOptions.ENABLE_UNALIGNED)),
            () -> assertEquals(Duration.ofMillis(1000L), flinkConfig.get(CheckpointingOptions.ALIGNED_CHECKPOINT_TIMEOUT)),
            () -> assertEquals("s3://bucket/checkpoints", flinkConfig.get(CheckpointingOptions.CHECKPOINTS_DIRECTORY))
        );
    }

    @Test
    @DisplayName("Should do nothing when checkpointing configuration is empty")
    void shouldDoNothingWhenEmpty() {
        Configuration flinkConfig = new Configuration();
        var customizer = new CheckpointingCustomizer(flinkConfig);
        var envProps = new ExecutionEnvironmentProperties(null, null, null, null, null, null, null);

        customizer.configure(envProps);

        assertNull(flinkConfig.get(CheckpointingOptions.CHECKPOINTING_INTERVAL));
    }
}
