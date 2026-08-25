package io.github.sekelenao.flinkboot.core.internal.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("DurationFormatter Tests")
class DurationFormatterTest {

    @Test
    @DisplayName("Private constructor should throw AssertionError")
    void testConstructorIsPrivate() throws Exception {
        var constructor = DurationFormatter.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
    }

    @Nested
    @DisplayName("Format Tests")
    class FormatTests {

        @Test
        @DisplayName("Should throw NullPointerException when duration is null")
        void shouldThrowNpeWhenDurationIsNull() {
            assertThrows(NullPointerException.class, () -> DurationFormatter.format(null));
        }

        @Test
        @DisplayName("Should format duration into millisecond string")
        void shouldFormatDurationIntoMillisString() {
            assertAll(
                () -> assertEquals("0ms", DurationFormatter.format(Duration.ZERO)),
                () -> assertEquals("50ms", DurationFormatter.format(Duration.ofMillis(50))),
                () -> assertEquals("1000ms", DurationFormatter.format(Duration.ofSeconds(1))),
                () -> assertEquals("60000ms", DurationFormatter.format(Duration.ofMinutes(1)))
            );
        }
    }
}
