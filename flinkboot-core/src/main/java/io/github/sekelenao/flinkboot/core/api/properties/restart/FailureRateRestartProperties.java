package io.github.sekelenao.flinkboot.core.api.properties.restart;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Configuration properties for failure rate restart strategy.
 */
public final class FailureRateRestartProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @Positive
    private final Integer maxFailuresPerInterval;

    @Positive
    private final Long failureIntervalMs;

    @PositiveOrZero
    private final Long delayMs;

    /**
     * Creates a new {@code FailureRateRestartProperties} instance.
     *
     * @param maxFailuresPerInterval maximum allowed failures within the interval before failing the job
     * @param failureIntervalMs      time interval window for measuring failure rate in milliseconds
     * @param delayMs                delay duration between restart attempts in milliseconds
     */
    @JsonCreator
    public FailureRateRestartProperties(
        @JsonProperty("max-failures-per-interval") Integer maxFailuresPerInterval,
        @JsonProperty("failure-interval-ms") Long failureIntervalMs,
        @JsonProperty("delay-ms") Long delayMs
    ) {
        this.maxFailuresPerInterval = maxFailuresPerInterval;
        this.failureIntervalMs = failureIntervalMs;
        this.delayMs = delayMs;
    }

    /**
     * Returns the maximum allowed failures within the failure interval.
     *
     * @return an {@link OptionalInt} containing max failures, or empty if not specified
     */
    public OptionalInt maxFailuresPerInterval() {
        if (maxFailuresPerInterval == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(maxFailuresPerInterval);
    }

    /**
     * Returns the failure measurement interval in milliseconds.
     *
     * @return an {@link OptionalLong} containing the interval in milliseconds, or empty if not specified
     */
    public OptionalLong failureIntervalMs() {
        if (failureIntervalMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(failureIntervalMs);
    }

    /**
     * Returns the delay duration between restart attempts in milliseconds.
     *
     * @return an {@link OptionalLong} containing the delay in milliseconds, or empty if not specified
     */
    public OptionalLong delayMs() {
        if (delayMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(delayMs);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof FailureRateRestartProperties)) {
            return false;
        }
        var o = (FailureRateRestartProperties) other;
        return Objects.equals(maxFailuresPerInterval, o.maxFailuresPerInterval)
            && Objects.equals(failureIntervalMs, o.failureIntervalMs)
            && Objects.equals(delayMs, o.delayMs);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(maxFailuresPerInterval, failureIntervalMs, delayMs);
    }

    @Override
    @Generated
    public String toString() {
        return "FailureRateRestartProperties{" +
            "maxFailuresPerInterval=" + maxFailuresPerInterval +
            ", failureIntervalMs=" + failureIntervalMs +
            ", delayMs=" + delayMs +
            '}';
    }
}
