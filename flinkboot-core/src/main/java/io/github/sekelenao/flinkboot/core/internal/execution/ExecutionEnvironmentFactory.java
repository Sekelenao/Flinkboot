package io.github.sekelenao.flinkboot.core.internal.execution;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.JobConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.checkpointing.CheckpointingConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.execution.ExecutionConfiguration;
import io.github.sekelenao.flinkboot.core.internal.execution.provider.ExecutionEnvironmentProvider;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.ExecutionOptions;
import org.apache.flink.configuration.ExternalizedCheckpointRetention;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.core.execution.CheckpointingMode;
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
        return provider.createEnvironment(configuration);
    }

    private void apply(ExecutionConfiguration execConfig) {
        execConfig.runtimeMode().ifPresent(mode -> configuration.set(ExecutionOptions.RUNTIME_MODE, RuntimeExecutionMode.valueOf(mode.name())));
        execConfig.parallelism().ifPresent(parallelism -> configuration.set(CoreOptions.DEFAULT_PARALLELISM, parallelism));
        execConfig.maxParallelism().ifPresent(maxParallelism -> configuration.set(PipelineOptions.MAX_PARALLELISM, maxParallelism));
        execConfig.bufferTimeoutMs().ifPresent(timeout -> configuration.set(ExecutionOptions.BUFFER_TIMEOUT, Duration.ofMillis(timeout)));
        execConfig.autoWatermarkIntervalMs().ifPresent(interval -> configuration.set(PipelineOptions.AUTO_WATERMARK_INTERVAL, Duration.ofMillis(interval)));
        execConfig.objectReuse().ifPresent(objectReuse -> configuration.set(PipelineOptions.OBJECT_REUSE, objectReuse));
    }

    private void apply(CheckpointingConfiguration checkpointingConfig) {
        checkpointingConfig.intervalMs().ifPresent(interval -> configuration.set(CheckpointingOptions.CHECKPOINTING_INTERVAL, Duration.ofMillis(interval)));
        checkpointingConfig.mode().ifPresent(mode -> configuration.set(CheckpointingOptions.CHECKPOINTING_CONSISTENCY_MODE, CheckpointingMode.valueOf(mode.name())));
        checkpointingConfig.timeoutMs().ifPresent(timeout -> configuration.set(CheckpointingOptions.CHECKPOINTING_TIMEOUT, Duration.ofMillis(timeout)));
        checkpointingConfig.minPauseBetweenCheckpointsMs().ifPresent(pause -> configuration.set(CheckpointingOptions.MIN_PAUSE_BETWEEN_CHECKPOINTS, Duration.ofMillis(pause)));
        checkpointingConfig.maxConcurrentCheckpoints().ifPresent(max -> configuration.set(CheckpointingOptions.MAX_CONCURRENT_CHECKPOINTS, max));
        checkpointingConfig.externalizedCheckpointCleanup().ifPresent(cleanup -> configuration.set(CheckpointingOptions.EXTERNALIZED_CHECKPOINT_RETENTION, ExternalizedCheckpointRetention.valueOf(cleanup.name())));
        checkpointingConfig.unalignedCheckpoints().ifPresent(unaligned -> configuration.set(CheckpointingOptions.ENABLE_UNALIGNED, unaligned));
        checkpointingConfig.alignedCheckpointTimeoutMs().ifPresent(timeout -> configuration.set(CheckpointingOptions.ALIGNED_CHECKPOINT_TIMEOUT, Duration.ofMillis(timeout)));
        checkpointingConfig.storageUri().ifPresent(uri -> configuration.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, uri));
    }
}
