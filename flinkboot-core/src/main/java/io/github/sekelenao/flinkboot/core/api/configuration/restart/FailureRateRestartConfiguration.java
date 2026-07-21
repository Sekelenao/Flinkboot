package io.github.sekelenao.flinkboot.core.api.configuration.restart;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class FailureRateRestartConfiguration {

    @Positive
    private final Integer maxFailuresPerInterval;

    @Positive
    private final Long failureIntervalMs;

    @PositiveOrZero
    private final Long delayMs;

    @JsonCreator
    public FailureRateRestartConfiguration(
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FailureRateRestartConfiguration that = (FailureRateRestartConfiguration) o;
        return Objects.equals(maxFailuresPerInterval, that.maxFailuresPerInterval) &&
            Objects.equals(failureIntervalMs, that.failureIntervalMs) &&
            Objects.equals(delayMs, that.delayMs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxFailuresPerInterval, failureIntervalMs, delayMs);
    }
}
