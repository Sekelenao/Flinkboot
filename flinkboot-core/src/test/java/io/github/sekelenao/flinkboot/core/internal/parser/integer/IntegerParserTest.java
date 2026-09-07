package io.github.sekelenao.flinkboot.core.internal.parser.integer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("IntegerParser")
class IntegerParserTest {

    @Nested
    @DisplayName("Parse")
    class Parse {

        @ParameterizedTest
        @ValueSource(strings = {"-2147483648", "-42", "-1", "0", "1", "42", "2147483647"})
        @DisplayName("Should parse valid integers")
        void shouldParseValidIntegers(String input) {
            assertEquals(Integer.parseInt(input), IntegerParser.parse(input, IllegalArgumentException::new));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"not-a-number"})
        @DisplayName("Should use the supplied exception for invalid values")
        void shouldRejectInvalidValues(String input) {
            var exception = new IllegalArgumentException("invalid integer");
            assertEquals(exception, assertThrows(
                IllegalArgumentException.class,
                () -> IntegerParser.parse(input, () -> exception)
            ));
        }

        @Test
        @DisplayName("Should reject a null exception supplier")
        void shouldRejectNullExceptionSupplier() {
            assertThrows(NullPointerException.class, () -> IntegerParser.parse("1", null));
        }
    }

    @Nested
    @DisplayName("ParseStrictlyPositive")
    class ParseStrictlyPositive {

        @ParameterizedTest
        @ValueSource(strings = {"1", "42", "2147483647"})
        @DisplayName("Should parse strictly positive integers")
        void shouldParseStrictlyPositiveIntegers(String input) {
            assertEquals(Integer.parseInt(input), IntegerParser.parseStrictlyPositive(input, IllegalArgumentException::new));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"not-a-number", "0", "-1"})
        @DisplayName("Should reject values that are not strictly positive integers")
        void shouldRejectValuesThatAreNotStrictlyPositiveIntegers(String input) {
            assertThrows(
                IllegalArgumentException.class,
                () -> IntegerParser.parseStrictlyPositive(input, IllegalArgumentException::new)
            );
        }

        @Test
        @DisplayName("Should reject a null exception supplier")
        void shouldRejectNullExceptionSupplier() {
            assertThrows(NullPointerException.class, () -> IntegerParser.parseStrictlyPositive("1", null));
        }
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw AssertionError when instantiated via reflection")
        void shouldPreventInstantiation() throws Exception {
            var constructor = IntegerParser.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
            assertInstanceOf(AssertionError.class, exception.getCause());
        }
    }
}
