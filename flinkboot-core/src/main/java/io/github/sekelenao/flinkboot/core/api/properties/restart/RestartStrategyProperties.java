package io.github.sekelenao.flinkboot.core.api.properties.restart;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.api.exception.configuration.InvalidRestartStrategyPropertiesException;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.Valid;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Root configuration properties for Flink job failure restart strategies.
 * <p>
 * Supports {@link RestartStrategyType#NO_RESTART}, {@link RestartStrategyType#FIXED_DELAY},
 * {@link RestartStrategyType#FAILURE_RATE}, {@link RestartStrategyType#EXPONENTIAL_DELAY},
 * or {@link RestartStrategyType#FALLBACK}.
 */
public final class RestartStrategyProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private final RestartStrategyType type;

    @Valid
    private final FixedDelayRestartProperties fixedDelay;

    @Valid
    private final FailureRateRestartProperties failureRate;

    @Valid
    private final ExponentialDelayRestartProperties exponentialDelay;

    /**
     * Creates a new {@code RestartStrategyProperties} instance.
     *
     * @param type             the restart strategy type (NO_RESTART, FIXED_DELAY, FAILURE_RATE, EXPONENTIAL_DELAY, FALLBACK)
     * @param fixedDelay       parameters for fixed delay restart strategy
     * @param failureRate      parameters for failure rate restart strategy
     * @param exponentialDelay parameters for exponential delay restart strategy
     * @throws InvalidRestartStrategyPropertiesException if parameters conflict with the selected type
     */
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

    /**
     * Returns the optional restart strategy type.
     *
     * @return an {@link Optional} containing the {@link RestartStrategyType}, or empty if not specified
     */
    public Optional<RestartStrategyType> type() {
        return Optional.ofNullable(type);
    }

    /**
     * Returns the fixed delay restart strategy properties.
     *
     * @return an {@link Optional} containing {@link FixedDelayRestartProperties}, or empty if not configured
     */
    public Optional<FixedDelayRestartProperties> fixedDelay() {
        return Optional.ofNullable(fixedDelay);
    }

    /**
     * Returns the failure rate restart strategy properties.
     *
     * @return an {@link Optional} containing {@link FailureRateRestartProperties}, or empty if not configured
     */
    public Optional<FailureRateRestartProperties> failureRate() {
        return Optional.ofNullable(failureRate);
    }

    /**
     * Returns the exponential delay restart strategy properties.
     *
     * @return an {@link Optional} containing {@link ExponentialDelayRestartProperties}, or empty if not configured
     */
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
