package io.github.sekelenao.flinkboot.core.api.properties.restart;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.time.DurationMin;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Configuration properties for failure rate restart strategy.
 */
public final class FailureRateRestartProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @Positive
    private final Integer maxFailuresPerInterval;

    @DurationMin(millis = 1)
    private final Duration failureInterval;

    @DurationMin(millis = 0)
    private final Duration delay;

    /**
     * Creates a new {@code FailureRateRestartProperties} instance.
     *
     * @param maxFailuresPerInterval maximum allowed failures within the interval before failing the job
     * @param failureInterval        time interval window for measuring failure rate
     * @param delay                  delay duration between restart attempts
     */
    @JsonCreator
    public FailureRateRestartProperties(
        @JsonProperty("max-failures-per-interval") Integer maxFailuresPerInterval,
        @JsonProperty("failure-interval") Duration failureInterval,
        @JsonProperty("delay") Duration delay
    ) {
        this.maxFailuresPerInterval = maxFailuresPerInterval;
        this.failureInterval = failureInterval;
        this.delay = delay;
    }

    /**
     * Returns the maximum allowed failures within the failure interval.
     *
     * @return an {@link OptionalInt} containing max failures, or empty if not specified
     */
    public OptionalInt maxFailuresPerInterval() {
        if (maxFailuresPerInterval == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(maxFailuresPerInterval);
    }

    /**
     * Returns the failure measurement interval duration.
     *
     * @return an {@link Optional} containing the interval duration, or empty if not specified
     */
    public Optional<Duration> failureInterval() {
        return Optional.ofNullable(failureInterval);
    }

    /**
     * Returns the delay duration between restart attempts.
     *
     * @return an {@link Optional} containing the delay duration, or empty if not specified
     */
    public Optional<Duration> delay() {
        return Optional.ofNullable(delay);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof FailureRateRestartProperties)) {
            return false;
        }
        var o = (FailureRateRestartProperties) other;
        return Objects.equals(maxFailuresPerInterval, o.maxFailuresPerInterval)
            && Objects.equals(failureInterval, o.failureInterval)
            && Objects.equals(delay, o.delay);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(maxFailuresPerInterval, failureInterval, delay);
    }

    @Override
    @Generated
    public String toString() {
        return "FailureRateRestartProperties{" +
            "maxFailuresPerInterval=" + maxFailuresPerInterval +
            ", failureInterval=" + failureInterval +
            ", delay=" + delay +
            '}';
    }
}

