package io.github.sekelenao.flinkboot.core.api.properties.restart;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidRestartStrategyPropertiesException;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.Valid;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class RestartStrategyProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private final RestartStrategyType type;

    @Valid
    private final FixedDelayRestartProperties fixedDelay;

    @Valid
    private final FailureRateRestartProperties failureRate;

    @Valid
    private final ExponentialDelayRestartProperties exponentialDelay;

    @JsonCreator
    public RestartStrategyProperties(
        @JsonProperty("type") RestartStrategyType type,
        @JsonProperty("fixed-delay") FixedDelayRestartProperties fixedDelay,
        @JsonProperty("failure-rate") FailureRateRestartProperties failureRate,
        @JsonProperty("exponential-delay") ExponentialDelayRestartProperties exponentialDelay
    ) {
        this.type = type;
        this.fixedDelay = fixedDelay;
        this.failureRate = failureRate;
        this.exponentialDelay = exponentialDelay;
        validate();
    }

    public Optional<RestartStrategyType> type() {
        return Optional.ofNullable(type);
    }

    public Optional<FixedDelayRestartProperties> fixedDelay() {
        return Optional.ofNullable(fixedDelay);
    }

    public Optional<FailureRateRestartProperties> failureRate() {
        return Optional.ofNullable(failureRate);
    }

    public Optional<ExponentialDelayRestartProperties> exponentialDelay() {
        return Optional.ofNullable(exponentialDelay);
    }

    private void validate() {
        RestartStrategyType effectiveType = type().orElse(RestartStrategyType.FALLBACK);

        if (effectiveType == RestartStrategyType.FALLBACK || effectiveType == RestartStrategyType.NO_RESTART) {
            if (fixedDelay != null || failureRate != null || exponentialDelay != null) {
                throw new InvalidRestartStrategyPropertiesException(
                    "No sub-configuration (fixed-delay, failure-rate, exponential-delay) must be specified when restart strategy type is " + effectiveType
                );
            }
        } else if (effectiveType == RestartStrategyType.FIXED_DELAY) {
            if (failureRate != null || exponentialDelay != null) {
                throw new InvalidRestartStrategyPropertiesException(
                    "Cannot specify failure-rate or exponential-delay when restart strategy type is FIXED_DELAY"
                );
            }
        } else if (effectiveType == RestartStrategyType.FAILURE_RATE) {
            if (fixedDelay != null || exponentialDelay != null) {
                throw new InvalidRestartStrategyPropertiesException(
                    "Cannot specify fixed-delay or exponential-delay when restart strategy type is FAILURE_RATE"
                );
            }
        } else if (effectiveType == RestartStrategyType.EXPONENTIAL_DELAY) {
            if (fixedDelay != null || failureRate != null) {
                throw new InvalidRestartStrategyPropertiesException(
                    "Cannot specify fixed-delay or failure-rate when restart strategy type is EXPONENTIAL_DELAY"
                );
            }
            if (exponentialDelay != null && exponentialDelay.initialBackoffMs().isPresent() && exponentialDelay.maxBackoffMs().isPresent()) {
                if (exponentialDelay.maxBackoffMs().getAsLong() < exponentialDelay.initialBackoffMs().getAsLong()) {
                    throw new InvalidRestartStrategyPropertiesException(
                        "max-backoff-ms cannot be smaller than initial-backoff-ms in exponential-delay restart strategy"
                    );
                }
            }
        }
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof RestartStrategyProperties)) {
            return false;
        }
        var o = (RestartStrategyProperties) other;
        return type == o.type
            && Objects.equals(fixedDelay, o.fixedDelay)
            && Objects.equals(failureRate, o.failureRate)
            && Objects.equals(exponentialDelay, o.exponentialDelay);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(type, fixedDelay, failureRate, exponentialDelay);
    }

    @Override
    @Generated
    public String toString() {
        return "RestartStrategyProperties{" +
            "type=" + type +
            ", fixedDelay=" + fixedDelay +
            ", failureRate=" + failureRate +
            ", exponentialDelay=" + exponentialDelay +
            '}';
    }
}
