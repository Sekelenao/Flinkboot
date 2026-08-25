package io.github.sekelenao.flinkboot.core.api.properties.restart;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Configuration properties for exponential delay restart strategy.
 */
public final class ExponentialDelayRestartProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Duration initialBackoff;

    private final Duration maxBackoff;

    @DecimalMin("1.0")
    private final Double backoffMultiplier;

    private final Duration resetBackoffThreshold;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private final Double jitterFactor;

    /**
     * Creates a new {@code ExponentialDelayRestartProperties} instance.
     *
     * @param initialBackoff        starting backoff delay duration
     * @param maxBackoff            maximum backoff delay upper bound duration
     * @param backoffMultiplier     multiplier for increasing backoff delay after each failure
     * @param resetBackoffThreshold duration of stable execution before resetting backoff delay
     * @param jitterFactor          jitter randomization factor (0.0 to 1.0)
     */
    @JsonCreator
    public ExponentialDelayRestartProperties(
        @JsonProperty("initial-backoff") Duration initialBackoff,
        @JsonProperty("max-backoff") Duration maxBackoff,
        @JsonProperty("backoff-multiplier") Double backoffMultiplier,
        @JsonProperty("reset-backoff-threshold") Duration resetBackoffThreshold,
        @JsonProperty("jitter-factor") Double jitterFactor
    ) {
        this.initialBackoff = initialBackoff;
        this.maxBackoff = maxBackoff;
        this.backoffMultiplier = backoffMultiplier;
        this.resetBackoffThreshold = resetBackoffThreshold;
        this.jitterFactor = jitterFactor;
    }

    /**
     * Returns the initial backoff delay duration.
     *
     * @return an {@link Optional} containing initial backoff duration, or empty if not specified
     */
    public Optional<Duration> initialBackoff() {
        return Optional.ofNullable(initialBackoff);
    }

    /**
     * Returns the maximum backoff delay cap duration.
     *
     * @return an {@link Optional} containing max backoff duration, or empty if not specified
     */
    public Optional<Duration> maxBackoff() {
        return Optional.ofNullable(maxBackoff);
    }

    /**
     * Returns the exponential backoff multiplier factor.
     *
     * @return an {@link OptionalDouble} containing backoff multiplier, or empty if not specified
     */
    public OptionalDouble backoffMultiplier() {
        if (backoffMultiplier == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(backoffMultiplier);
    }

    /**
     * Returns the threshold duration after which the backoff delay resets.
     *
     * @return an {@link Optional} containing reset threshold duration, or empty if not specified
     */
    public Optional<Duration> resetBackoffThreshold() {
        return Optional.ofNullable(resetBackoffThreshold);
    }

    /**
     * Returns the jitter randomization factor applied to delay intervals.
     *
     * @return an {@link OptionalDouble} containing jitter factor, or empty if not specified
     */
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
        return Objects.equals(initialBackoff, o.initialBackoff)
            && Objects.equals(maxBackoff, o.maxBackoff)
            && Objects.equals(backoffMultiplier, o.backoffMultiplier)
            && Objects.equals(resetBackoffThreshold, o.resetBackoffThreshold)
            && Objects.equals(jitterFactor, o.jitterFactor);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(
            initialBackoff,
            maxBackoff,
            backoffMultiplier,
            resetBackoffThreshold,
            jitterFactor
        );
    }

    @Override
    @Generated
    public String toString() {
        return "ExponentialDelayRestartProperties{" +
            "initialBackoff=" + initialBackoff +
            ", maxBackoff=" + maxBackoff +
            ", backoffMultiplier=" + backoffMultiplier +
            ", resetBackoffThreshold=" + resetBackoffThreshold +
            ", jitterFactor=" + jitterFactor +
            '}';
    }
}

