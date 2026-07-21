package io.github.sekelenao.flinkboot.core.api.configuration.execution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class ExecutionConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ExecutionRuntimeMode runtimeMode;

    @Positive
    private final Integer parallelism;

    @Positive
    private final Integer maxParallelism;

    @PositiveOrZero
    private final Long bufferTimeoutMs;

    @PositiveOrZero
    private final Long autoWatermarkIntervalMs;

    private final Boolean objectReuse;

    @JsonCreator
    public ExecutionConfiguration(
        @JsonProperty("runtime-mode") ExecutionRuntimeMode runtimeMode,
        @JsonProperty("parallelism") Integer parallelism,
        @JsonProperty("max-parallelism") Integer maxParallelism,
        @JsonProperty("buffer-timeout-ms") Long bufferTimeoutMs,
        @JsonProperty("auto-watermark-interval-ms") Long autoWatermarkIntervalMs,
        @JsonProperty("object-reuse") Boolean objectReuse
    ) {
        this.runtimeMode = runtimeMode;
        this.parallelism = parallelism;
        this.maxParallelism = maxParallelism;
        this.bufferTimeoutMs = bufferTimeoutMs;
        this.autoWatermarkIntervalMs = autoWatermarkIntervalMs;
        this.objectReuse = objectReuse;
    }

    public Optional<ExecutionRuntimeMode> runtimeMode() {
        return Optional.ofNullable(runtimeMode);
    }

    public OptionalInt parallelism() {
        if (parallelism == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(parallelism);
    }

    public OptionalInt maxParallelism() {
        if (maxParallelism == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(maxParallelism);
    }

    public OptionalLong bufferTimeoutMs() {
        if (bufferTimeoutMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(bufferTimeoutMs);
    }

    public OptionalLong autoWatermarkIntervalMs() {
        if (autoWatermarkIntervalMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(autoWatermarkIntervalMs);
    }

    public Optional<Boolean> objectReuse() {
        return Optional.ofNullable(objectReuse);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof ExecutionConfiguration)) {
            return false;
        }
        var o = (ExecutionConfiguration) other;
        return runtimeMode == o.runtimeMode
            && Objects.equals(parallelism, o.parallelism)
            && Objects.equals(maxParallelism, o.maxParallelism)
            && Objects.equals(bufferTimeoutMs, o.bufferTimeoutMs)
            && Objects.equals(autoWatermarkIntervalMs, o.autoWatermarkIntervalMs)
            && Objects.equals(objectReuse, o.objectReuse);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(runtimeMode, parallelism, maxParallelism, bufferTimeoutMs, autoWatermarkIntervalMs, objectReuse);
    }

    @Override
    @Generated
    public String toString() {
        return "ExecutionConfiguration{" +
            "runtimeMode=" + runtimeMode +
            ", parallelism=" + parallelism +
            ", maxParallelism=" + maxParallelism +
            ", bufferTimeoutMs=" + bufferTimeoutMs +
            ", autoWatermarkIntervalMs=" + autoWatermarkIntervalMs +
            ", objectReuse=" + objectReuse +
            '}';
    }
}
