package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingMode;
import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.ExternalizedCheckpointCleanupMode;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ExternalizedCheckpointRetention;

import java.time.Duration;
import java.util.Objects;

public final class CheckpointingCustomizer implements EnvironmentCustomizer {

    private final Configuration toConfigure;

    public CheckpointingCustomizer(Configuration toConfigure) {
        this.toConfigure = Objects.requireNonNull(toConfigure);
    }

    @Override
    public void configure(ExecutionEnvironmentProperties configuration) {
        Objects.requireNonNull(configuration);
        configuration.checkpointing().ifPresent(this::apply);
    }

    private void apply(CheckpointingProperties checkpointingConfig) {
        checkpointingConfig.intervalMs().ifPresent(this::applyIntervalMs);
        checkpointingConfig.mode().ifPresent(this::applyMode);
        checkpointingConfig.timeoutMs().ifPresent(this::applyTimeoutMs);
        checkpointingConfig.minPauseBetweenCheckpointsMs().ifPresent(this::applyMinPauseBetweenCheckpointsMs);
        checkpointingConfig.maxConcurrentCheckpoints().ifPresent(this::applyMaxConcurrentCheckpoints);
        checkpointingConfig.externalizedCheckpointCleanup().ifPresent(this::applyExternalizedCheckpointCleanup);
        checkpointingConfig.unalignedCheckpoints().ifPresent(this::applyUnalignedCheckpoints);
        checkpointingConfig.alignedCheckpointTimeoutMs().ifPresent(this::applyAlignedCheckpointTimeoutMs);
        checkpointingConfig.storageUri().ifPresent(this::applyStorageUri);
    }

    private void applyIntervalMs(long intervalMs) {
        var duration = Duration.ofMillis(intervalMs);
        toConfigure.set(CheckpointingOptions.CHECKPOINTING_INTERVAL, duration);
    }

    private void applyMode(CheckpointingMode mode) {
        var flinkMode = org.apache.flink.core.execution.CheckpointingMode.valueOf(mode.toString());
        toConfigure.set(CheckpointingOptions.CHECKPOINTING_CONSISTENCY_MODE, flinkMode);
    }

    private void applyTimeoutMs(long timeoutMs) {
        var duration = Duration.ofMillis(timeoutMs);
        toConfigure.set(CheckpointingOptions.CHECKPOINTING_TIMEOUT, duration);
    }

    private void applyMinPauseBetweenCheckpointsMs(long minPauseBetweenCheckpointsMs) {
        var duration = Duration.ofMillis(minPauseBetweenCheckpointsMs);
        toConfigure.set(CheckpointingOptions.MIN_PAUSE_BETWEEN_CHECKPOINTS, duration);
    }

    private void applyMaxConcurrentCheckpoints(int maxConcurrentCheckpoints) {
        toConfigure.set(CheckpointingOptions.MAX_CONCURRENT_CHECKPOINTS, maxConcurrentCheckpoints);
    }

    private void applyExternalizedCheckpointCleanup(ExternalizedCheckpointCleanupMode externalizedCheckpointCleanup) {
        var retention = ExternalizedCheckpointRetention.valueOf(externalizedCheckpointCleanup.toString());
        toConfigure.set(CheckpointingOptions.EXTERNALIZED_CHECKPOINT_RETENTION, retention);
    }

    private void applyUnalignedCheckpoints(boolean unalignedCheckpoints) {
        toConfigure.set(CheckpointingOptions.ENABLE_UNALIGNED, unalignedCheckpoints);
    }

    private void applyAlignedCheckpointTimeoutMs(long alignedCheckpointTimeoutMs) {
        var duration = Duration.ofMillis(alignedCheckpointTimeoutMs);
        toConfigure.set(CheckpointingOptions.ALIGNED_CHECKPOINT_TIMEOUT, duration);
    }

    private void applyStorageUri(String storageUri) {
        toConfigure.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, storageUri);
    }
}
