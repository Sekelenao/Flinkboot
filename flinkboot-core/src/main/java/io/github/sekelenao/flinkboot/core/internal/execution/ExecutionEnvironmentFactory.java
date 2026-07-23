package io.github.sekelenao.flinkboot.core.internal.execution;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.JobConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.checkpointing.CheckpointingConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.execution.ExecutionConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.restart.ExponentialDelayRestartConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.restart.FailureRateRestartConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.restart.FixedDelayRestartConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.restart.RestartStrategyConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.savepoint.SavepointRestoreConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.state.StateBackendConfiguration;
import io.github.sekelenao.flinkboot.core.internal.execution.provider.ExecutionEnvironmentProvider;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.ExecutionOptions;
import org.apache.flink.configuration.ExternalizedCheckpointRetention;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.configuration.StateBackendOptions;
import org.apache.flink.configuration.StateRecoveryOptions;
import org.apache.flink.core.execution.CheckpointingMode;
import org.apache.flink.core.execution.RestoreMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;
import java.util.Objects;

public final class ExecutionEnvironmentFactory {

    private final Configuration configuration;
    private final ExecutionEnvironmentProvider provider;

    public ExecutionEnvironmentFactory(ExecutionEnvironmentProvider provider) {
        this.configuration = new Configuration();
        this.provider = Objects.requireNonNull(provider);
    }

    public StreamExecutionEnvironment create(JobConfiguration jobConfiguration) {
        Objects.requireNonNull(jobConfiguration);
        configuration.set(PipelineOptions.NAME, jobConfiguration.name());
        jobConfiguration.environment()
            .flatMap(ExecutionEnvironmentConfiguration::execution)
            .ifPresent(this::apply);
        jobConfiguration.environment()
            .flatMap(ExecutionEnvironmentConfiguration::checkpointing)
            .ifPresent(this::apply);
        jobConfiguration.environment()
            .flatMap(ExecutionEnvironmentConfiguration::restartStrategy)
            .ifPresent(this::apply);
        jobConfiguration.environment()
            .flatMap(ExecutionEnvironmentConfiguration::stateBackend)
            .ifPresent(this::apply);
        jobConfiguration.environment()
            .flatMap(ExecutionEnvironmentConfiguration::savepointRestore)
            .ifPresent(this::apply);
        return provider.createEnvironment(configuration);
    }

    private void apply(ExecutionConfiguration execConfig) {
        execConfig.runtimeMode().ifPresent(mode -> configuration.set(ExecutionOptions.RUNTIME_MODE, RuntimeExecutionMode.valueOf(mode.toString())));
        execConfig.parallelism().ifPresent(parallelism -> configuration.set(CoreOptions.DEFAULT_PARALLELISM, parallelism));
        execConfig.maxParallelism().ifPresent(maxParallelism -> configuration.set(PipelineOptions.MAX_PARALLELISM, maxParallelism));
        execConfig.bufferTimeoutMs().ifPresent(timeout -> configuration.set(ExecutionOptions.BUFFER_TIMEOUT, Duration.ofMillis(timeout)));
        execConfig.autoWatermarkIntervalMs().ifPresent(interval -> configuration.set(PipelineOptions.AUTO_WATERMARK_INTERVAL, Duration.ofMillis(interval)));
        execConfig.objectReuse().ifPresent(objectReuse -> configuration.set(PipelineOptions.OBJECT_REUSE, objectReuse));
    }

    private void apply(CheckpointingConfiguration checkpointingConfig) {
        checkpointingConfig.intervalMs().ifPresent(interval -> configuration.set(CheckpointingOptions.CHECKPOINTING_INTERVAL, Duration.ofMillis(interval)));
        checkpointingConfig.mode().ifPresent(mode -> configuration.set(CheckpointingOptions.CHECKPOINTING_CONSISTENCY_MODE, CheckpointingMode.valueOf(mode.toString())));
        checkpointingConfig.timeoutMs().ifPresent(timeout -> configuration.set(CheckpointingOptions.CHECKPOINTING_TIMEOUT, Duration.ofMillis(timeout)));
        checkpointingConfig.minPauseBetweenCheckpointsMs().ifPresent(pause -> configuration.set(CheckpointingOptions.MIN_PAUSE_BETWEEN_CHECKPOINTS, Duration.ofMillis(pause)));
        checkpointingConfig.maxConcurrentCheckpoints().ifPresent(max -> configuration.set(CheckpointingOptions.MAX_CONCURRENT_CHECKPOINTS, max));
        checkpointingConfig.externalizedCheckpointCleanup().ifPresent(cleanup -> configuration.set(CheckpointingOptions.EXTERNALIZED_CHECKPOINT_RETENTION, ExternalizedCheckpointRetention.valueOf(cleanup.toString())));
        checkpointingConfig.unalignedCheckpoints().ifPresent(unaligned -> configuration.set(CheckpointingOptions.ENABLE_UNALIGNED, unaligned));
        checkpointingConfig.alignedCheckpointTimeoutMs().ifPresent(timeout -> configuration.set(CheckpointingOptions.ALIGNED_CHECKPOINT_TIMEOUT, Duration.ofMillis(timeout)));
        checkpointingConfig.storageUri().ifPresent(uri -> configuration.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, uri));
    }

    private void apply(RestartStrategyConfiguration restartConfig) {
        restartConfig.type().ifPresent(type -> {
            switch (type) {
                case NO_RESTART:
                    configuration.set(RestartStrategyOptions.RESTART_STRATEGY, "none");
                    break;
                case FIXED_DELAY:
                    configuration.set(RestartStrategyOptions.RESTART_STRATEGY, "fixed-delay");
                    restartConfig.fixedDelay().ifPresent(this::apply);
                    break;
                case FAILURE_RATE:
                    configuration.set(RestartStrategyOptions.RESTART_STRATEGY, "failure-rate");
                    restartConfig.failureRate().ifPresent(this::apply);
                    break;
                case EXPONENTIAL_DELAY:
                    configuration.set(RestartStrategyOptions.RESTART_STRATEGY, "exponential-delay");
                    restartConfig.exponentialDelay().ifPresent(this::apply);
                    break;
                case FALLBACK:
                    break;
            }
        });
    }

    private void apply(FixedDelayRestartConfiguration config) {
        config.attempts().ifPresent(attempts -> configuration.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, attempts));
        config.delayMs().ifPresent(delay -> configuration.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, Duration.ofMillis(delay)));
    }

    private void apply(FailureRateRestartConfiguration config) {
        config.maxFailuresPerInterval().ifPresent(max -> configuration.set(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_MAX_FAILURES_PER_INTERVAL, max));
        config.failureIntervalMs().ifPresent(interval -> configuration.set(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_FAILURE_RATE_INTERVAL, Duration.ofMillis(interval)));
        config.delayMs().ifPresent(delay -> configuration.set(RestartStrategyOptions.RESTART_STRATEGY_FAILURE_RATE_DELAY, Duration.ofMillis(delay)));
    }

    private void apply(ExponentialDelayRestartConfiguration config) {
        config.initialBackoffMs().ifPresent(initial -> configuration.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_INITIAL_BACKOFF, Duration.ofMillis(initial)));
        config.maxBackoffMs().ifPresent(max -> configuration.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_MAX_BACKOFF, Duration.ofMillis(max)));
        config.backoffMultiplier().ifPresent(multiplier -> configuration.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_BACKOFF_MULTIPLIER, multiplier));
        config.resetBackoffThresholdMs().ifPresent(threshold -> configuration.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_RESET_BACKOFF_THRESHOLD, Duration.ofMillis(threshold)));
        config.jitterFactor().ifPresent(jitter -> configuration.set(RestartStrategyOptions.RESTART_STRATEGY_EXPONENTIAL_DELAY_JITTER_FACTOR, jitter));
    }

    private void apply(StateBackendConfiguration stateConfig) {
        stateConfig.type().ifPresent(type -> {
            switch (type) {
                case HASHMAP:
                    configuration.set(StateBackendOptions.STATE_BACKEND, "hashmap");
                    break;
                case ROCKSDB:
                    configuration.set(StateBackendOptions.STATE_BACKEND, "rocksdb");
                    break;
                case CHANGELOG:
                    configuration.set(StateBackendOptions.STATE_BACKEND, "changelog");
                    break;
                case CUSTOM:
                    stateConfig.customClass().ifPresent(customClass -> configuration.set(StateBackendOptions.STATE_BACKEND, customClass));
                    break;
            }
        });
        stateConfig.checkpointStorage().ifPresent(storage -> configuration.set(CheckpointingOptions.CHECKPOINT_STORAGE, storage.toString().toLowerCase()));
        stateConfig.storagePath().ifPresent(path -> configuration.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, path));
        stateConfig.incremental().ifPresent(incremental -> configuration.set(CheckpointingOptions.INCREMENTAL_CHECKPOINTS, incremental));
        stateConfig.latencyTracking().ifPresent(tracking -> configuration.set(StateBackendOptions.LATENCY_TRACK_ENABLED, tracking));
    }

    private void apply(SavepointRestoreConfiguration savepointConfig) {
        configuration.set(StateRecoveryOptions.SAVEPOINT_PATH, savepointConfig.savepointPath());
        savepointConfig.allowNonRestoredState().ifPresent(allow -> configuration.set(StateRecoveryOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE, allow));
        savepointConfig.restoreMode().ifPresent(mode -> configuration.set(StateRecoveryOptions.RESTORE_MODE, RestoreMode.valueOf(mode.toString())));
    }
}
