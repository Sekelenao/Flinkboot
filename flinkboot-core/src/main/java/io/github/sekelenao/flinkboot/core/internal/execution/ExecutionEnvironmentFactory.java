package io.github.sekelenao.flinkboot.core.internal.execution;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.JobConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.execution.ExecutionConfiguration;
import io.github.sekelenao.flinkboot.core.internal.execution.provider.ExecutionEnvironmentProvider;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.ExecutionOptions;
import org.apache.flink.configuration.PipelineOptions;
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

    public Configuration createFlinkConfiguration(JobConfiguration jobConfiguration) {
        Objects.requireNonNull(jobConfiguration);
        configuration.set(PipelineOptions.NAME, jobConfiguration.name());
        jobConfiguration.environment()
            .flatMap(ExecutionEnvironmentConfiguration::execution)
            .ifPresent(this::applyExecutionConfiguration);
        return configuration;
    }

    public StreamExecutionEnvironment createExecutionEnvironment(JobConfiguration jobConfiguration) {
        Configuration flinkConfig = createFlinkConfiguration(jobConfiguration);
        return provider.createEnvironment(flinkConfig);
    }

    private void applyExecutionConfiguration(ExecutionConfiguration execConfig) {
        execConfig.runtimeMode().ifPresent(mode ->
            configuration.set(ExecutionOptions.RUNTIME_MODE, RuntimeExecutionMode.valueOf(mode.name()))
        );

        execConfig.parallelism().ifPresent(parallelism -> configuration.set(CoreOptions.DEFAULT_PARALLELISM, parallelism));

        execConfig.maxParallelism().ifPresent(maxParallelism ->
            configuration.set(PipelineOptions.MAX_PARALLELISM, maxParallelism)
        );

        execConfig.bufferTimeoutMs().ifPresent(timeout ->
            configuration.set(ExecutionOptions.BUFFER_TIMEOUT, Duration.ofMillis(timeout))
        );

        execConfig.autoWatermarkIntervalMs().ifPresent(interval ->
            configuration.set(PipelineOptions.AUTO_WATERMARK_INTERVAL, Duration.ofMillis(interval))
        );

        execConfig.objectReuse().ifPresent(objectReuse ->
            configuration.set(PipelineOptions.OBJECT_REUSE, objectReuse)
        );
    }
}
