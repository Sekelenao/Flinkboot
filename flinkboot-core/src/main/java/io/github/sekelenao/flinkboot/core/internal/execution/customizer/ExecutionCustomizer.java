package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.configuration.ExecutionEnvironmentConfiguration;
import io.github.sekelenao.flinkboot.core.api.configuration.execution.ExecutionConfiguration;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.ExecutionOptions;
import org.apache.flink.configuration.PipelineOptions;

import java.time.Duration;
import java.util.Objects;

public final class ExecutionCustomizer implements EnvironmentCustomizer {

    private final Configuration toConfigure;

    public ExecutionCustomizer(Configuration toConfigure) {
        this.toConfigure = Objects.requireNonNull(toConfigure);
    }

    @Override
    public void configure(ExecutionEnvironmentConfiguration configuration) {
        Objects.requireNonNull(configuration);
        configuration.execution().ifPresent(this::apply);
    }

    private void apply(ExecutionConfiguration execConfig) {
        this.applyRuntimeMode(execConfig);
        execConfig.parallelism().ifPresent(this::applyParallelism);
        execConfig.maxParallelism().ifPresent(this::applyMaxParallelism);
        execConfig.bufferTimeoutMs().ifPresent(this::applyBufferTimeoutMs);
        execConfig.autoWatermarkIntervalMs().ifPresent(this::applyAutoWatermarkIntervalMs);
        execConfig.objectReuse().ifPresent(this::applyObjectReuse);
    }

    private void applyRuntimeMode(ExecutionConfiguration execConfig) {
        execConfig.runtimeMode().ifPresent(mode ->
            toConfigure.set(ExecutionOptions.RUNTIME_MODE, RuntimeExecutionMode.valueOf(mode.toString()))
        );
    }

    private void applyParallelism(int parallelism) {
        toConfigure.set(CoreOptions.DEFAULT_PARALLELISM, parallelism);
    }

    private void applyMaxParallelism(int maxParallelism) {
        toConfigure.set(PipelineOptions.MAX_PARALLELISM, maxParallelism);
    }

    private void applyBufferTimeoutMs(long bufferTimeoutMs) {
        toConfigure.set(ExecutionOptions.BUFFER_TIMEOUT, Duration.ofMillis(bufferTimeoutMs));
    }

    private void applyAutoWatermarkIntervalMs(long autoWatermarkIntervalMs) {
        toConfigure.set(PipelineOptions.AUTO_WATERMARK_INTERVAL, Duration.ofMillis(autoWatermarkIntervalMs));
    }

    private void applyObjectReuse(boolean objectReuse) {
        toConfigure.set(PipelineOptions.OBJECT_REUSE, objectReuse);
    }
}
