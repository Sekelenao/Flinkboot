package io.github.sekelenao.flinkboot.core.internal.parser.bool;

import io.github.sekelenao.flinkboot.core.api.exception.parsing.BooleanParsingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StrictBooleanParser")
class StrictBooleanParserTest {

    @Test
    @DisplayName("Should parse valid boolean values case-insensitively")
    void shouldParseValidBooleans() {
        assertAll(
            () -> assertTrue(StrictBooleanParser.parse("true")),
            () -> assertTrue(StrictBooleanParser.parse("TRUE")),
            () -> assertTrue(StrictBooleanParser.parse("TrUe")),
            
            () -> assertFalse(StrictBooleanParser.parse("false")),
            () -> assertFalse(StrictBooleanParser.parse("FALSE")),
            () -> assertFalse(StrictBooleanParser.parse("FaLsE"))
        );
    }

    @Test
    @DisplayName("Should throw BooleanParsingException for invalid boolean values")
    void shouldThrowExceptionForInvalidBooleans() {
        assertAll(
            () -> assertThrows(BooleanParsingException.class, () -> StrictBooleanParser.parse("yes")),
            () -> assertThrows(BooleanParsingException.class, () -> StrictBooleanParser.parse("no")),
            () -> assertThrows(BooleanParsingException.class, () -> StrictBooleanParser.parse("1")),
            () -> assertThrows(BooleanParsingException.class, () -> StrictBooleanParser.parse("0")),
            () -> assertThrows(BooleanParsingException.class, () -> StrictBooleanParser.parse("")),
            () -> assertThrows(BooleanParsingException.class, () -> StrictBooleanParser.parse("   "))
        );
    }

    @Test
    @DisplayName("Should throw AssertionError when trying to instantiate the class via reflection")
    void shouldPreventInstantiation() throws Exception {
        var constructor = StrictBooleanParser.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
    }
}
