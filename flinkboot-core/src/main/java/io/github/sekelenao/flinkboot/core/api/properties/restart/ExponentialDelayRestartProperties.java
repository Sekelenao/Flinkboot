package io.github.sekelenao.flinkboot.core.api.properties.restart;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalLong;

/**
 * Configuration properties for exponential delay restart strategy.
 */
public final class ExponentialDelayRestartProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @Positive
    private final Long initialBackoffMs;

    @Positive
    private final Long maxBackoffMs;

    @DecimalMin("1.0")
    private final Double backoffMultiplier;

    @Positive
    private final Long resetBackoffThresholdMs;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private final Double jitterFactor;

    /**
     * Creates a new {@code ExponentialDelayRestartProperties} instance.
     *
     * @param initialBackoffMs        starting backoff delay in milliseconds
     * @param maxBackoffMs            maximum backoff delay upper bound in milliseconds
     * @param backoffMultiplier       multiplier for increasing backoff delay after each failure
     * @param resetBackoffThresholdMs duration of stable execution before resetting backoff delay
     * @param jitterFactor            jitter randomization factor (0.0 to 1.0)
     */
    @JsonCreator
    public ExponentialDelayRestartProperties(
        @JsonProperty("initial-backoff-ms") Long initialBackoffMs,
        @JsonProperty("max-backoff-ms") Long maxBackoffMs,
        @JsonProperty("backoff-multiplier") Double backoffMultiplier,
        @JsonProperty("reset-backoff-threshold-ms") Long resetBackoffThresholdMs,
        @JsonProperty("jitter-factor") Double jitterFactor
    ) {
        this.initialBackoffMs = initialBackoffMs;
        this.maxBackoffMs = maxBackoffMs;
        this.backoffMultiplier = backoffMultiplier;
        this.resetBackoffThresholdMs = resetBackoffThresholdMs;
        this.jitterFactor = jitterFactor;
    }

    /**
     * Returns the initial backoff delay in milliseconds.
     *
     * @return an {@link OptionalLong} containing initial backoff, or empty if not specified
     */
    public OptionalLong initialBackoffMs() {
        if (initialBackoffMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(initialBackoffMs);
    }

    /**
     * Returns the maximum backoff delay cap in milliseconds.
     *
     * @return an {@link OptionalLong} containing max backoff, or empty if not specified
     */
    public OptionalLong maxBackoffMs() {
        if (maxBackoffMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(maxBackoffMs);
    }

    /**
     * Returns the exponential backoff multiplier factor.
     *
     * @return an {@link OptionalDouble} containing backoff multiplier, or empty if not specified
     */
    public OptionalDouble backoffMultiplier() {
        if (backoffMultiplier == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(backoffMultiplier);
    }

    /**
     * Returns the threshold in milliseconds after which the backoff delay resets.
     *
     * @return an {@link OptionalLong} containing reset threshold, or empty if not specified
     */
    public OptionalLong resetBackoffThresholdMs() {
        if (resetBackoffThresholdMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(resetBackoffThresholdMs);
    }

    /**
     * Returns the jitter randomization factor applied to delay intervals.
     *
     * @return an {@link OptionalDouble} containing jitter factor, or empty if not specified
     */
    public OptionalDouble jitterFactor() {
        if (jitterFactor == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(jitterFactor);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof ExponentialDelayRestartProperties)) {
            return false;
        }
        var o = (ExponentialDelayRestartProperties) other;
        return Objects.equals(initialBackoffMs, o.initialBackoffMs)
            && Objects.equals(maxBackoffMs, o.maxBackoffMs)
            && Objects.equals(backoffMultiplier, o.backoffMultiplier)
            && Objects.equals(resetBackoffThresholdMs, o.resetBackoffThresholdMs)
            && Objects.equals(jitterFactor, o.jitterFactor);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(
            initialBackoffMs,
            maxBackoffMs,
            backoffMultiplier,
            resetBackoffThresholdMs,
            jitterFactor
        );
    }

    @Override
    @Generated
    public String toString() {
        return "ExponentialDelayRestartProperties{" +
            "initialBackoffMs=" + initialBackoffMs +
            ", maxBackoffMs=" + maxBackoffMs +
            ", backoffMultiplier=" + backoffMultiplier +
            ", resetBackoffThresholdMs=" + resetBackoffThresholdMs +
            ", jitterFactor=" + jitterFactor +
            '}';
    }
}
