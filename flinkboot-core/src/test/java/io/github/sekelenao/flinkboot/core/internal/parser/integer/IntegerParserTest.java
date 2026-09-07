package io.github.sekelenao.flinkboot.core.internal.parser.integer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("IntegerParser")
class IntegerParserTest {

    @ParameterizedTest
    @ValueSource(strings = {"1", "42", "2147483647"})
    @DisplayName("Should parse valid integers")
    void shouldParseValidIntegers(String input) {
        assertEquals(Integer.parseInt(input), IntegerParser.parse(input, IllegalArgumentException::new));
    }

    @Test
    @DisplayName("Should use the supplied exception for non-numeric values")
    void shouldRejectNonNumericValues() {
        var exception = new IllegalArgumentException("invalid integer");
        assertEquals(exception, assertThrows(
            IllegalArgumentException.class,
            () -> IntegerParser.parse("not-a-number", () -> exception)
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    @DisplayName("Should reject integers that are not strictly positive")
    void shouldRejectNonPositiveIntegers(String input) {
        assertThrows(
            IllegalArgumentException.class,
            () -> IntegerParser.parseStrictlyPositive(input, IllegalArgumentException::new)
        );
    }

    @Test
    @DisplayName("Should throw AssertionError when instantiated via reflection")
    void shouldPreventInstantiation() throws Exception {
        var constructor = IntegerParser.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
    }
}
