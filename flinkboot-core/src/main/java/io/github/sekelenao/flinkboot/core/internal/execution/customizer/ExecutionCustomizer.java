package io.github.sekelenao.flinkboot.core.internal.execution.customizer;

import io.github.sekelenao.flinkboot.core.api.properties.ExecutionEnvironmentProperties;
import io.github.sekelenao.flinkboot.core.api.properties.execution.ExecutionProperties;
import io.github.sekelenao.flinkboot.core.api.properties.execution.ExecutionRuntimeMode;
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
    public void configure(ExecutionEnvironmentProperties configuration) {
        Objects.requireNonNull(configuration);
        configuration.execution().ifPresent(this::apply);
    }

    private void apply(ExecutionProperties execConfig) {
        execConfig.runtimeMode().ifPresent(this::applyRuntimeMode);
        execConfig.parallelism().ifPresent(this::applyParallelism);
        execConfig.maxParallelism().ifPresent(this::applyMaxParallelism);
        execConfig.bufferTimeoutMs().ifPresent(this::applyBufferTimeoutMs);
        execConfig.autoWatermarkIntervalMs().ifPresent(this::applyAutoWatermarkIntervalMs);
        execConfig.objectReuse().ifPresent(this::applyObjectReuse);
    }

    private void applyRuntimeMode(ExecutionRuntimeMode runtimeMode) {
        var mode = RuntimeExecutionMode.valueOf(runtimeMode.toString());
        toConfigure.set(ExecutionOptions.RUNTIME_MODE, mode);
    }

    private void applyParallelism(int parallelism) {
        toConfigure.set(CoreOptions.DEFAULT_PARALLELISM, parallelism);
    }

    private void applyMaxParallelism(int maxParallelism) {
        toConfigure.set(PipelineOptions.MAX_PARALLELISM, maxParallelism);
    }

    private void applyBufferTimeoutMs(long bufferTimeoutMs) {
        var duration = Duration.ofMillis(bufferTimeoutMs);
        toConfigure.set(ExecutionOptions.BUFFER_TIMEOUT, duration);
    }

    private void applyAutoWatermarkIntervalMs(long autoWatermarkIntervalMs) {
        var duration = Duration.ofMillis(autoWatermarkIntervalMs);
        toConfigure.set(PipelineOptions.AUTO_WATERMARK_INTERVAL, duration);
    }

    private void applyObjectReuse(boolean objectReuse) {
        toConfigure.set(PipelineOptions.OBJECT_REUSE, objectReuse);
    }
}
