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
