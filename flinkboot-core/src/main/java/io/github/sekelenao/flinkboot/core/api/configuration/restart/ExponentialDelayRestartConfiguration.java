package io.github.sekelenao.flinkboot.core.api.configuration.restart;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

public final class ExponentialDelayRestartConfiguration {

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

    @JsonCreator
    public ExponentialDelayRestartConfiguration(
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

    public OptionalLong initialBackoffMs() {
        if (initialBackoffMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(initialBackoffMs);
    }

    public OptionalLong maxBackoffMs() {
        if (maxBackoffMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(maxBackoffMs);
    }

    public OptionalDouble backoffMultiplier() {
        if (backoffMultiplier == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(backoffMultiplier);
    }

    public OptionalLong resetBackoffThresholdMs() {
        if (resetBackoffThresholdMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(resetBackoffThresholdMs);
    }

    public OptionalDouble jitterFactor() {
        if (jitterFactor == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(jitterFactor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExponentialDelayRestartConfiguration that = (ExponentialDelayRestartConfiguration) o;
        return Objects.equals(initialBackoffMs, that.initialBackoffMs) &&
            Objects.equals(maxBackoffMs, that.maxBackoffMs) &&
            Objects.equals(backoffMultiplier, that.backoffMultiplier) &&
            Objects.equals(resetBackoffThresholdMs, that.resetBackoffThresholdMs) &&
            Objects.equals(jitterFactor, that.jitterFactor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            initialBackoffMs,
            maxBackoffMs,
            backoffMultiplier,
            resetBackoffThresholdMs,
            jitterFactor
        );
    }
}
