package io.github.sekelenao.flinkboot.core.api.properties.execution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.time.DurationMin;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

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

    @DurationMin(millis = 0)
    private final Duration bufferTimeout;

    @DurationMin(millis = 0)
    private final Duration autoWatermarkInterval;

    private final Boolean objectReuse;

    /**
     * Creates a new {@code ExecutionProperties} instance.
     *
     * @param runtimeMode           execution runtime mode (STREAMING, BATCH, or AUTOMATIC)
     * @param parallelism           default operator parallelism
     * @param maxParallelism        maximum job parallelism
     * @param bufferTimeout         network buffer flush timeout duration
     * @param autoWatermarkInterval automatic watermark generation interval duration
     * @param objectReuse           whether object reuse is enabled
     */
    @JsonCreator
    public ExecutionProperties(
        @JsonProperty("runtime-mode") ExecutionRuntimeMode runtimeMode,
        @JsonProperty("parallelism") Integer parallelism,
        @JsonProperty("max-parallelism") Integer maxParallelism,
        @JsonProperty("buffer-timeout") Duration bufferTimeout,
        @JsonProperty("auto-watermark-interval") Duration autoWatermarkInterval,
        @JsonProperty("object-reuse") Boolean objectReuse
    ) {
        this.runtimeMode = runtimeMode;
        this.parallelism = parallelism;
        this.maxParallelism = maxParallelism;
        this.bufferTimeout = bufferTimeout;
        this.autoWatermarkInterval = autoWatermarkInterval;
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
     * Returns the optional buffer timeout duration.
     *
     * @return an {@link Optional} containing the buffer timeout duration, or empty if not specified
     */
    public Optional<Duration> bufferTimeout() {
        return Optional.ofNullable(bufferTimeout);
    }

    /**
     * Returns the optional automatic watermark generation interval duration.
     *
     * @return an {@link Optional} containing the interval duration, or empty if not specified
     */
    public Optional<Duration> autoWatermarkInterval() {
        return Optional.ofNullable(autoWatermarkInterval);
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
            && Objects.equals(bufferTimeout, o.bufferTimeout)
            && Objects.equals(autoWatermarkInterval, o.autoWatermarkInterval)
            && Objects.equals(objectReuse, o.objectReuse);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(runtimeMode, parallelism, maxParallelism, bufferTimeout, autoWatermarkInterval, objectReuse);
    }

    @Override
    @Generated
    public String toString() {
        return "ExecutionProperties{" +
            "runtimeMode=" + runtimeMode +
            ", parallelism=" + parallelism +
            ", maxParallelism=" + maxParallelism +
            ", bufferTimeout=" + bufferTimeout +
            ", autoWatermarkInterval=" + autoWatermarkInterval +
            ", objectReuse=" + objectReuse +
            '}';
    }
}

