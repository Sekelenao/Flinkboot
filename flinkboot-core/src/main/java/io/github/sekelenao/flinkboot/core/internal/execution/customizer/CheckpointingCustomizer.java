package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.checkpointing.CheckpointingConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.checkpointing.ExternalizedCheckpointCleanupMode;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ExternalizedCheckpointRetention;
import org.apache.flink.core.execution.CheckpointingMode;

import java.time.Duration;
import java.util.Objects;

public final class CheckpointingCustomizer implements EnvironmentCustomizer {

    private final Configuration toConfigure;

    public CheckpointingCustomizer(Configuration toConfigure) {
        this.toConfigure = Objects.requireNonNull(toConfigure);
    }

    @Override
    public void configure(ExecutionEnvironmentConfiguration configuration) {
        Objects.requireNonNull(configuration);
        configuration.checkpointing().ifPresent(this::apply);
    }

    private void apply(CheckpointingConfiguration checkpointingConfig) {
        checkpointingConfig.intervalMs().ifPresent(this::applyIntervalMs);
        this.applyMode(checkpointingConfig);
        checkpointingConfig.timeoutMs().ifPresent(this::applyTimeoutMs);
        checkpointingConfig.minPauseBetweenCheckpointsMs().ifPresent(this::applyMinPauseBetweenCheckpointsMs);
        checkpointingConfig.maxConcurrentCheckpoints().ifPresent(this::applyMaxConcurrentCheckpoints);
        checkpointingConfig.externalizedCheckpointCleanup().ifPresent(this::applyExternalizedCheckpointCleanup);
        checkpointingConfig.unalignedCheckpoints().ifPresent(this::applyUnalignedCheckpoints);
        checkpointingConfig.alignedCheckpointTimeoutMs().ifPresent(this::applyAlignedCheckpointTimeoutMs);
        checkpointingConfig.storageUri().ifPresent(this::applyStorageUri);
    }

    private void applyIntervalMs(long intervalMs) {
        toConfigure.set(CheckpointingOptions.CHECKPOINTING_INTERVAL, Duration.ofMillis(intervalMs));
    }

    private void applyMode(CheckpointingConfiguration checkpointingConfig) {
        checkpointingConfig.mode().ifPresent(mode ->
            toConfigure.set(CheckpointingOptions.CHECKPOINTING_CONSISTENCY_MODE, CheckpointingMode.valueOf(mode.toString()))
        );
    }

    private void applyTimeoutMs(long timeoutMs) {
        toConfigure.set(CheckpointingOptions.CHECKPOINTING_TIMEOUT, Duration.ofMillis(timeoutMs));
    }

    private void applyMinPauseBetweenCheckpointsMs(long minPauseBetweenCheckpointsMs) {
        toConfigure.set(CheckpointingOptions.MIN_PAUSE_BETWEEN_CHECKPOINTS, Duration.ofMillis(minPauseBetweenCheckpointsMs));
    }

    private void applyMaxConcurrentCheckpoints(int maxConcurrentCheckpoints) {
        toConfigure.set(CheckpointingOptions.MAX_CONCURRENT_CHECKPOINTS, maxConcurrentCheckpoints);
    }

    private void applyExternalizedCheckpointCleanup(ExternalizedCheckpointCleanupMode externalizedCheckpointCleanup) {
        toConfigure.set(CheckpointingOptions.EXTERNALIZED_CHECKPOINT_RETENTION, ExternalizedCheckpointRetention.valueOf(externalizedCheckpointCleanup.toString()));
    }

    private void applyUnalignedCheckpoints(boolean unalignedCheckpoints) {
        toConfigure.set(CheckpointingOptions.ENABLE_UNALIGNED, unalignedCheckpoints);
    }

    private void applyAlignedCheckpointTimeoutMs(long alignedCheckpointTimeoutMs) {
        toConfigure.set(CheckpointingOptions.ALIGNED_CHECKPOINT_TIMEOUT, Duration.ofMillis(alignedCheckpointTimeoutMs));
    }

    private void applyStorageUri(String storageUri) {
        toConfigure.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, storageUri);
    }
}
