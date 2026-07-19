package io.github.sekelenao.flinkboot.core.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class CheckpointingConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Min(1)
    private final Long interval;

    @Pattern(regexp = "EXACTLY_ONCE|AT_LEAST_ONCE")
    private final String mode;

    @Min(1)
    private final Long timeout;

    @Min(0)
    private final Long minPause;

    @Min(1)
    private final Integer maxConcurrent;

    @Pattern(regexp = "RETAIN_ON_CANCELLATION|DELETE_ON_CANCELLATION")
    private final String externalizedCleanup;

    @JsonCreator
    public CheckpointingConfiguration(
        @JsonProperty("interval") Long interval,
        @JsonProperty("mode") String mode,
        @JsonProperty("timeout") Long timeout,
        @JsonProperty("minPause") Long minPause,
        @JsonProperty("maxConcurrent") Integer maxConcurrent,
        @JsonProperty("externalizedCleanup") String externalizedCleanup
    ) {
        this.interval = Objects.requireNonNull(interval);
        this.mode = mode;
        this.timeout = timeout;
        this.minPause = minPause;
        this.maxConcurrent = maxConcurrent;
        this.externalizedCleanup = externalizedCleanup;
    }

    public long interval() {
        return interval;
    }

    public Optional<String> mode() {
        return Optional.ofNullable(mode);
    }

    public OptionalLong timeout() {
        if (timeout == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(timeout);
    }

    public OptionalLong minPause() {
        if (minPause == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(minPause);
    }

    public OptionalInt maxConcurrent() {
        if (maxConcurrent == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(maxConcurrent);
    }

    public Optional<String> externalizedCleanup() {
        return Optional.ofNullable(externalizedCleanup);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof CheckpointingConfiguration)) {
            return false;
        }
        var otherConfig = (CheckpointingConfiguration) other;
        return Objects.equals(interval, otherConfig.interval)
            && Objects.equals(mode, otherConfig.mode)
            && Objects.equals(timeout, otherConfig.timeout)
            && Objects.equals(minPause, otherConfig.minPause)
            && Objects.equals(maxConcurrent, otherConfig.maxConcurrent)
            && Objects.equals(externalizedCleanup, otherConfig.externalizedCleanup);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(interval, mode, timeout, minPause, maxConcurrent, externalizedCleanup);
    }

    @Override
    @Generated
    public String toString() {
        return "CheckpointingConfiguration{" +
            "interval=" + interval +
            ", mode='" + mode + '\'' +
            ", timeout=" + timeout +
            ", minPause=" + minPause +
            ", maxConcurrent=" + maxConcurrent +
            ", externalizedCleanup='" + externalizedCleanup + '\'' +
            '}';
    }
}
