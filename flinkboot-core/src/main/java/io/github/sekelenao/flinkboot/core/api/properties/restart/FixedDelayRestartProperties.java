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
 * Configuration properties for fixed delay restart strategy.
 */
public final class FixedDelayRestartProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @Positive
    private final Integer attempts;

    @DurationMin(millis = 0)
    private final Duration delay;

    /**
     * Creates a new {@code FixedDelayRestartProperties} instance.
     *
     * @param attempts maximum number of restart attempts
     * @param delay    delay duration between restart attempts
     */
    @JsonCreator
    public FixedDelayRestartProperties(
        @JsonProperty("attempts") Integer attempts,
        @JsonProperty("delay") Duration delay
    ) {
        this.attempts = attempts;
        this.delay = delay;
    }

    /**
     * Returns the maximum number of restart attempts.
     *
     * @return an {@link OptionalInt} containing the attempt count, or empty if not specified
     */
    public OptionalInt attempts() {
        if (attempts == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(attempts);
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
        if (!(other instanceof FixedDelayRestartProperties)) {
            return false;
        }
        var o = (FixedDelayRestartProperties) other;
        return Objects.equals(attempts, o.attempts)
            && Objects.equals(delay, o.delay);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(attempts, delay);
    }

    @Override
    @Generated
    public String toString() {
        return "FixedDelayRestartProperties{" +
            "attempts=" + attempts +
            ", delay=" + delay +
            '}';
    }
}

