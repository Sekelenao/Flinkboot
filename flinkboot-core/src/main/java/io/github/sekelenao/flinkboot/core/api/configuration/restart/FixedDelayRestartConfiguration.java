package io.github.sekelenao.flinkboot.core.api.configuration.restart;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.OptionalLong;

public final class FixedDelayRestartConfiguration {

    @Positive
    private final Integer attempts;

    @PositiveOrZero
    private final Long delayMs;

    @JsonCreator
    public FixedDelayRestartConfiguration(
        @JsonProperty("attempts") Integer attempts,
        @JsonProperty("delay-ms") Long delayMs
    ) {
        this.attempts = attempts;
        this.delayMs = delayMs;
    }

    public OptionalInt attempts() {
        if (attempts == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(attempts);
    }

    public OptionalLong delayMs() {
        if (delayMs == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(delayMs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FixedDelayRestartConfiguration that = (FixedDelayRestartConfiguration) o;
        return Objects.equals(attempts, that.attempts) &&
            Objects.equals(delayMs, that.delayMs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attempts, delayMs);
    }
}
