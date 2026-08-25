package io.github.sekelenao.flinkboot.core.internal.time;

import java.time.Duration;
import java.util.Objects;

/**
 * Internal utility for formatting {@link Duration} instances into connector configuration option strings.
 */
public final class DurationFormatter {

    private DurationFormatter() {
        throw new AssertionError("You cannot instantiate this class");
    }

    /**
     * Formats a {@link Duration} into a standard millisecond duration string (e.g. {@code "50ms"}).
     *
     * @param duration the duration to format
     * @return the formatted duration string
     * @throws NullPointerException if {@code duration} is {@code null}
     */
    public static String format(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        return duration.toMillis() + "ms";
    }
}
