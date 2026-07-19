package io.github.sekelenao.flinkboot.core.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

public final class ExponentialDelayRestartConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Min(1)
    private final Long initialBackoff;

    @NotNull
    @Min(1)
    private final Long maxBackoff;

    @NotNull
    @Min(1)
    private final Double backoffMultiplier;

    @JsonCreator
    public ExponentialDelayRestartConfiguration(
        @JsonProperty("initialBackoff") Long initialBackoff,
        @JsonProperty("maxBackoff") Long maxBackoff,
        @JsonProperty("backoffMultiplier") Double backoffMultiplier
    ) {
        this.initialBackoff = Objects.requireNonNull(initialBackoff);
        this.maxBackoff = Objects.requireNonNull(maxBackoff);
        this.backoffMultiplier = Objects.requireNonNull(backoffMultiplier);
    }

    public long initialBackoff() {
        return initialBackoff;
    }

    public long maxBackoff() {
        return maxBackoff;
    }

    public double backoffMultiplier() {
        return backoffMultiplier;
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof ExponentialDelayRestartConfiguration)) {
            return false;
        }
        var otherConfig = (ExponentialDelayRestartConfiguration) other;
        return Objects.equals(initialBackoff, otherConfig.initialBackoff)
            && Objects.equals(maxBackoff, otherConfig.maxBackoff)
            && Objects.equals(backoffMultiplier, otherConfig.backoffMultiplier);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(initialBackoff, maxBackoff, backoffMultiplier);
    }

    @Override
    @Generated
    public String toString() {
        return "ExponentialDelayRestartConfiguration{" +
            "initialBackoff=" + initialBackoff +
            ", maxBackoff=" + maxBackoff +
            ", backoffMultiplier=" + backoffMultiplier +
            '}';
    }
}
