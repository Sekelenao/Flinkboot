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

public final class FailureRateRestartProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @Positive
    private final Integer maxFailuresPerInterval;

    @Positive
    private final Long failureIntervalMs;

    @PositiveOrZero
    private final Long delayMs;

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

    public OptionalInt maxFailuresPerInterval() {
        if (maxFailuresPerInterval == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(maxFailuresPerInterval);
    }

    public OptionalLong failureIntervalMs() {
        if (failureIntervalMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(failureIntervalMs);
    }

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
