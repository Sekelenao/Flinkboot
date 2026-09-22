package io.github.sekelenao.flinkboot.core.api.exception.parsing;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("IntegerParsingException")
class IntegerParsingExceptionTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should create exception with message and verify FlinkbootException inheritance")
        void shouldCreateExceptionWithMessage() {
            var message = "Invalid integer format";
            var exception = new IntegerParsingException(message);

            assertAll(
                () -> assertInstanceOf(FlinkbootException.class, exception),
                () -> assertEquals(message, exception.getMessage()),
                () -> assertNull(exception.getCause())
            );
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            var message = "Integer parsing failure";
            var cause = new NumberFormatException("For input string: \"abc\"");
            var exception = new IntegerParsingException(message, cause);

            assertAll(
                () -> assertInstanceOf(FlinkbootException.class, exception),
                () -> assertEquals(message, exception.getMessage()),
                () -> assertSame(cause, exception.getCause())
            );
        }
    }
}
