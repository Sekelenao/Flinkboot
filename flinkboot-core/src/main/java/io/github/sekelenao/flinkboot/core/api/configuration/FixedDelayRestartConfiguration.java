package io.github.sekelenao.flinkboot.core.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

public final class FixedDelayRestartConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Min(1)
    private final Integer attempts;

    @NotNull
    @Min(0)
    private final Long delay;

    @JsonCreator
    public FixedDelayRestartConfiguration(
        @JsonProperty("attempts") Integer attempts,
        @JsonProperty("delay") Long delay
    ) {
        this.attempts = Objects.requireNonNull(attempts);
        this.delay = Objects.requireNonNull(delay);
    }

    public int attempts() {
        return attempts;
    }

    public long delay() {
        return delay;
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof FixedDelayRestartConfiguration)) {
            return false;
        }
        var otherConfig = (FixedDelayRestartConfiguration) other;
        return Objects.equals(attempts, otherConfig.attempts)
            && Objects.equals(delay, otherConfig.delay);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(attempts, delay);
    }

    @Override
    @Generated
    public String toString() {
        return "FixedDelayRestartConfiguration{" +
            "attempts=" + attempts +
            ", delay=" + delay +
            '}';
    }
}
