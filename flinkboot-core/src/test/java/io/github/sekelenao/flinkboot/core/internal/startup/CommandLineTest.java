package io.github.sekelenao.flinkboot.core.internal.startup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CommandLine")
class CommandLineTest {

    @Nested
    @DisplayName("Parse")
    class Parse {

        @Test
        @DisplayName("Should throw NullPointerException when args array is null")
        void shouldThrowExceptionWhenArgsIsNull() {
            assertThrows(NullPointerException.class, () -> CommandLine.parse(null));
        }

        @Test
        @DisplayName("Should parse empty arguments list")
        void shouldParseEmptyArgs() {
            var cmd = CommandLine.parse(new String[0]);
            assertAll(
                () -> assertTrue(cmd.option("any").isEmpty()),
                () -> assertFalse(cmd.flag("any"))
            );
        }

        @Test
        @DisplayName("Should parse simple option")
        void shouldParseSimpleOption() {
            String[] args = {"-key", "value"};
            var cmd = CommandLine.parse(args);
            assertAll(
                () -> assertEquals("value", cmd.option("key").orElseThrow()),
                () -> assertFalse(cmd.flag("key"))
            );
        }

        @Test
        @DisplayName("Should parse simple flag")
        void shouldParseSimpleFlag() {
            String[] args = {"--verbose"};
            var cmd = CommandLine.parse(args);
            assertAll(
                () -> assertTrue(cmd.flag("verbose")),
                () -> assertTrue(cmd.option("verbose").isEmpty())
            );
        }

        @Test
        @DisplayName("Should throw NoSuchElementException when option is missing its value")
        void shouldThrowExceptionWhenOptionMissingValue() {
            String[] args = {"-key"};
            assertThrows(NoSuchElementException.class, () -> CommandLine.parse(args));
        }

        @Test
        @DisplayName("Should parse multiple options and flags")
        void shouldParseMultipleOptionsAndFlags() {
            String[] args = {"-key1", "value1", "--verbose", "-key2", "value2", "--debug"};
            var cmd = CommandLine.parse(args);
            assertAll(
                () -> assertEquals("value1", cmd.option("key1").orElseThrow()),
                () -> assertEquals("value2", cmd.option("key2").orElseThrow()),
                () -> assertTrue(cmd.flag("verbose")),
                () -> assertTrue(cmd.flag("debug"))
            );
        }

        @Test
        @DisplayName("Should parse and retrieve options and flags case-insensitively")
        void shouldParseCaseInsensitively() {
            String[] args = {"-KeY", "Value", "--VeRbOsE"};
            var cmd = CommandLine.parse(args);
            assertAll(
                () -> assertEquals("Value", cmd.option("key").orElseThrow()),
                () -> assertEquals("Value", cmd.option("KEY").orElseThrow()),
                () -> assertEquals("Value", cmd.option("KeY").orElseThrow()),
                () -> assertTrue(cmd.flag("verbose")),
                () -> assertTrue(cmd.flag("VERBOSE")),
                () -> assertTrue(cmd.flag("VeRbOsE"))
            );
        }

        @Test
        @DisplayName("Should ignore single hyphen and double hyphen arguments")
        void shouldIgnoreSingleAndDoubleHyphens() {
            String[] args = {"-", "--"};
            var cmd = CommandLine.parse(args);
            assertAll(
                () -> assertTrue(cmd.option("").isEmpty()),
                () -> assertFalse(cmd.flag(""))
            );
        }
    }
}
