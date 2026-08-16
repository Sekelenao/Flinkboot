package io.github.sekelenao.flinkboot.core.api.properties.execution;

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

/**
 * Configuration properties controlling Flink execution runtime behaviors, parallelism,
 * buffer timeouts, watermark intervals, and object reuse.
 */
public final class ExecutionProperties implements Serializable {

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

    /**
     * Creates a new {@code ExecutionProperties} instance.
     *
     * @param runtimeMode             execution runtime mode (STREAMING, BATCH, or AUTOMATIC)
     * @param parallelism             default operator parallelism
     * @param maxParallelism          maximum job parallelism
     * @param bufferTimeoutMs         network buffer flush timeout in milliseconds
     * @param autoWatermarkIntervalMs automatic watermark generation interval in milliseconds
     * @param objectReuse             whether object reuse is enabled
     */
    @JsonCreator
    public ExecutionProperties(
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

    /**
     * Returns the optional execution runtime mode.
     *
     * @return an {@link Optional} containing the runtime mode, or empty if not specified
     */
    public Optional<ExecutionRuntimeMode> runtimeMode() {
        return Optional.ofNullable(runtimeMode);
    }

    /**
     * Returns the optional default parallelism.
     *
     * @return an {@link OptionalInt} containing the parallelism, or empty if not specified
     */
    public OptionalInt parallelism() {
        if (parallelism == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(parallelism);
    }

    /**
     * Returns the optional maximum parallelism.
     *
     * @return an {@link OptionalInt} containing the maximum parallelism, or empty if not specified
     */
    public OptionalInt maxParallelism() {
        if (maxParallelism == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(maxParallelism);
    }

    /**
     * Returns the optional buffer timeout in milliseconds.
     *
     * @return an {@link OptionalLong} containing the buffer timeout in milliseconds, or empty if not specified
     */
    public OptionalLong bufferTimeoutMs() {
        if (bufferTimeoutMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(bufferTimeoutMs);
    }

    /**
     * Returns the optional automatic watermark generation interval in milliseconds.
     *
     * @return an {@link OptionalLong} containing the interval in milliseconds, or empty if not specified
     */
    public OptionalLong autoWatermarkIntervalMs() {
        if (autoWatermarkIntervalMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(autoWatermarkIntervalMs);
    }

    /**
     * Returns whether object reuse is enabled.
     *
     * @return an {@link Optional} containing the object reuse flag, or empty if not specified
     */
    public Optional<Boolean> objectReuse() {
        return Optional.ofNullable(objectReuse);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof ExecutionProperties)) {
            return false;
        }
        var o = (ExecutionProperties) other;
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
        return "ExecutionProperties{" +
            "runtimeMode=" + runtimeMode +
            ", parallelism=" + parallelism +
            ", maxParallelism=" + maxParallelism +
            ", bufferTimeoutMs=" + bufferTimeoutMs +
            ", autoWatermarkIntervalMs=" + autoWatermarkIntervalMs +
            ", objectReuse=" + objectReuse +
            '}';
    }
}
