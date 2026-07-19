package io.github.sekelenao.flinkboot.core.api.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class RestartStrategyConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Pattern(regexp = "none|fixed-delay|failure-rate|exponential-delay")
    private final String type;

    @Valid
    private final FixedDelayRestartConfiguration fixedDelay;

    @Valid
    private final FailureRateRestartConfiguration failureRate;

    @Valid
    private final ExponentialDelayRestartConfiguration exponentialDelay;

    @JsonCreator
    public RestartStrategyConfiguration(
        @JsonProperty("type") String type,
        @JsonProperty("fixedDelay") FixedDelayRestartConfiguration fixedDelay,
        @JsonProperty("failureRate") FailureRateRestartConfiguration failureRate,
        @JsonProperty("exponentialDelay") ExponentialDelayRestartConfiguration exponentialDelay
    ) {
        this.type = Objects.requireNonNull(type);
        this.fixedDelay = fixedDelay;
        this.failureRate = failureRate;
        this.exponentialDelay = exponentialDelay;
    }

    public String type() {
        return type;
    }

    public Optional<FixedDelayRestartConfiguration> fixedDelay() {
        return Optional.ofNullable(fixedDelay);
    }

    public Optional<FailureRateRestartConfiguration> failureRate() {
        return Optional.ofNullable(failureRate);
    }

    public Optional<ExponentialDelayRestartConfiguration> exponentialDelay() {
        return Optional.ofNullable(exponentialDelay);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof RestartStrategyConfiguration)) {
            return false;
        }
        var otherConfig = (RestartStrategyConfiguration) other;
        return Objects.equals(type, otherConfig.type)
            && Objects.equals(fixedDelay, otherConfig.fixedDelay)
            && Objects.equals(failureRate, otherConfig.failureRate)
            && Objects.equals(exponentialDelay, otherConfig.exponentialDelay);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(type, fixedDelay, failureRate, exponentialDelay);
    }

    @Override
    @Generated
    public String toString() {
        return "RestartStrategyConfiguration{" +
            "type='" + type + '\'' +
            ", fixedDelay=" + fixedDelay +
            ", failureRate=" + failureRate +
            ", exponentialDelay=" + exponentialDelay +
            '}';
    }
}
