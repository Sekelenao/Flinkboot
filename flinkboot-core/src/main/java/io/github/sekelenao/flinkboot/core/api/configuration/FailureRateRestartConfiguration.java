package io.github.sekelenao.flinkboot.core.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

public final class FailureRateRestartConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Min(1)
    private final Integer maxFailuresPerInterval;

    @NotNull
    @Min(1)
    private final Long failureRateInterval;

    @NotNull
    @Min(0)
    private final Long delay;

    @JsonCreator
    public FailureRateRestartConfiguration(
        @JsonProperty("maxFailuresPerInterval") Integer maxFailuresPerInterval,
        @JsonProperty("failureRateInterval") Long failureRateInterval,
        @JsonProperty("delay") Long delay
    ) {
        this.maxFailuresPerInterval = Objects.requireNonNull(maxFailuresPerInterval);
        this.failureRateInterval = Objects.requireNonNull(failureRateInterval);
        this.delay = Objects.requireNonNull(delay);
    }

    public int maxFailuresPerInterval() {
        return maxFailuresPerInterval;
    }

    public long failureRateInterval() {
        return failureRateInterval;
    }

    public long delay() {
        return delay;
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof FailureRateRestartConfiguration)) {
            return false;
        }
        var otherConfig = (FailureRateRestartConfiguration) other;
        return Objects.equals(maxFailuresPerInterval, otherConfig.maxFailuresPerInterval)
            && Objects.equals(failureRateInterval, otherConfig.failureRateInterval)
            && Objects.equals(delay, otherConfig.delay);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(maxFailuresPerInterval, failureRateInterval, delay);
    }

    @Override
    @Generated
    public String toString() {
        return "FailureRateRestartConfiguration{" +
            "maxFailuresPerInterval=" + maxFailuresPerInterval +
            ", failureRateInterval=" + failureRateInterval +
            ", delay=" + delay +
            '}';
    }
}
