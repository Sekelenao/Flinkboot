package io.github.sekelenao.flinkboot.core.api.properties.restart;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Configuration properties for fixed delay restart strategy.
 */
public final class FixedDelayRestartProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @Positive
    private final Integer attempts;

    @PositiveOrZero
    private final Long delayMs;

    /**
     * Creates a new {@code FixedDelayRestartProperties} instance.
     *
     * @param attempts maximum number of restart attempts
     * @param delayMs  delay duration between restart attempts in milliseconds
     */
    @JsonCreator
    public FixedDelayRestartProperties(
        @JsonProperty("attempts") Integer attempts,
        @JsonProperty("delay-ms") Long delayMs
    ) {
        this.attempts = attempts;
        this.delayMs = delayMs;
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
     * Returns the delay duration between restart attempts in milliseconds.
     *
     * @return an {@link OptionalLong} containing the delay in milliseconds, or empty if not specified
     */
    public OptionalLong delayMs() {
        if (delayMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(delayMs);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof FixedDelayRestartProperties)) {
            return false;
        }
        var o = (FixedDelayRestartProperties) other;
        return Objects.equals(attempts, o.attempts)
            && Objects.equals(delayMs, o.delayMs);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(attempts, delayMs);
    }

    @Override
    @Generated
    public String toString() {
        return "FixedDelayRestartProperties{" +
            "attempts=" + attempts +
            ", delayMs=" + delayMs +
            '}';
    }
}
