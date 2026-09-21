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

@DisplayName("UnresolvedPropertyPlaceholderException")
class UnresolvedPropertyPlaceholderExceptionTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should create exception with message and verify hierarchy inheritance")
        void shouldCreateExceptionWithMessage() {
            var message = "Unresolved placeholder: ${MISSING_VAR}";
            var exception = new UnresolvedPropertyPlaceholderException(message);

            assertAll(
                () -> assertInstanceOf(YamlParsingException.class, exception),
                () -> assertInstanceOf(FlinkbootException.class, exception),
                () -> assertEquals(message, exception.getMessage()),
                () -> assertNull(exception.getCause())
            );
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            var message = "Placeholder resolution error";
            var cause = new RuntimeException("Env error");
            var exception = new UnresolvedPropertyPlaceholderException(message, cause);

            assertAll(
                () -> assertInstanceOf(YamlParsingException.class, exception),
                () -> assertInstanceOf(FlinkbootException.class, exception),
                () -> assertEquals(message, exception.getMessage()),
                () -> assertSame(cause, exception.getCause())
            );
        }
    }
}
