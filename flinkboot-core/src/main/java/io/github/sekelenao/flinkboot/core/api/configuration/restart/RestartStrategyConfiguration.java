package io.github.sekelenao.flinkboot.core.api.configuration.restart;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidRestartStrategyConfigurationException;
import jakarta.validation.Valid;

import java.util.Objects;
import java.util.Optional;

public final class RestartStrategyConfiguration {

    private final RestartStrategyType type;

    @Valid
    private final FixedDelayRestartConfiguration fixedDelay;

    @Valid
    private final FailureRateRestartConfiguration failureRate;

    @Valid
    private final ExponentialDelayRestartConfiguration exponentialDelay;

    @JsonCreator
    public RestartStrategyConfiguration(
        @JsonProperty("type") RestartStrategyType type,
        @JsonProperty("fixed-delay") FixedDelayRestartConfiguration fixedDelay,
        @JsonProperty("failure-rate") FailureRateRestartConfiguration failureRate,
        @JsonProperty("exponential-delay") ExponentialDelayRestartConfiguration exponentialDelay
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

    public Optional<FixedDelayRestartConfiguration> fixedDelay() {
        return Optional.ofNullable(fixedDelay);
    }

    public Optional<FailureRateRestartConfiguration> failureRate() {
        return Optional.ofNullable(failureRate);
    }

    public Optional<ExponentialDelayRestartConfiguration> exponentialDelay() {
        return Optional.ofNullable(exponentialDelay);
    }

    private void validate() {
        RestartStrategyType effectiveType = type().orElse(RestartStrategyType.FALLBACK);

        if (effectiveType == RestartStrategyType.FALLBACK || effectiveType == RestartStrategyType.NO_RESTART) {
            if (fixedDelay != null || failureRate != null || exponentialDelay != null) {
                throw new InvalidRestartStrategyConfigurationException(
                    "No sub-configuration (fixed-delay, failure-rate, exponential-delay) must be specified when restart strategy type is " + effectiveType
                );
            }
        } else if (effectiveType == RestartStrategyType.FIXED_DELAY) {
            if (failureRate != null || exponentialDelay != null) {
                throw new InvalidRestartStrategyConfigurationException(
                    "Cannot specify failure-rate or exponential-delay when restart strategy type is FIXED_DELAY"
                );
            }
        } else if (effectiveType == RestartStrategyType.FAILURE_RATE) {
            if (fixedDelay != null || exponentialDelay != null) {
                throw new InvalidRestartStrategyConfigurationException(
                    "Cannot specify fixed-delay or exponential-delay when restart strategy type is FAILURE_RATE"
                );
            }
        } else if (effectiveType == RestartStrategyType.EXPONENTIAL_DELAY) {
            if (fixedDelay != null || failureRate != null) {
                throw new InvalidRestartStrategyConfigurationException(
                    "Cannot specify fixed-delay or failure-rate when restart strategy type is EXPONENTIAL_DELAY"
                );
            }
            if (exponentialDelay != null && exponentialDelay.initialBackoffMs().isPresent() && exponentialDelay.maxBackoffMs().isPresent()) {
                if (exponentialDelay.maxBackoffMs().getAsLong() < exponentialDelay.initialBackoffMs().getAsLong()) {
                    throw new InvalidRestartStrategyConfigurationException(
                        "max-backoff-ms cannot be smaller than initial-backoff-ms in exponential-delay restart strategy"
                    );
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestartStrategyConfiguration that = (RestartStrategyConfiguration) o;
        return type == that.type &&
            Objects.equals(fixedDelay, that.fixedDelay) &&
            Objects.equals(failureRate, that.failureRate) &&
            Objects.equals(exponentialDelay, that.exponentialDelay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, fixedDelay, failureRate, exponentialDelay);
    }
}
