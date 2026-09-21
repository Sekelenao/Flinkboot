package io.github.sekelenao.flinkboot.core.internal.startup;

import io.github.sekelenao.flinkboot.core.api.exception.parsing.CommandLineParsingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

        @ParameterizedTest(name = "Argument at index {0} is null")
        @ValueSource(ints = {0, 1, 2, 3, 4})
        @DisplayName("Should throw CommandLineParsingException when argument at any index is null")
        void shouldThrowExceptionWhenArgumentAtIndexIsNull(int nullIndex) {
            String[] args = {"run", "-key", "value", "--verbose", "job"};
            args[nullIndex] = null;

            var exception = assertThrows(
                    CommandLineParsingException.class,
                    () -> CommandLine.parse(args)
            );

            assertEquals("Argument at index " + nullIndex + " must not be null.", exception.getMessage());
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
        @DisplayName("Should throw CommandLineParsingException when option is missing its value")
        void shouldThrowExceptionWhenOptionMissingValue() {
            String[] args = {"-key"};
            var exception = assertThrows(CommandLineParsingException.class, () -> CommandLine.parse(args));
            assertEquals("Option '-key' requires a value.", exception.getMessage());
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

        @Test
        @DisplayName("Should ignore arguments without a leading hyphen")
        void shouldIgnoreArgumentsWithoutLeadingHyphen() {
            String[] args = {"run", "job"};
            var cmd = CommandLine.parse(args);
            assertAll(
                () -> assertTrue(cmd.option("run").isEmpty()),
                () -> assertFalse(cmd.flag("run")),
                () -> assertTrue(cmd.option("job").isEmpty()),
                () -> assertFalse(cmd.flag("job")),
                () -> assertTrue(cmd.option("any").isEmpty()),
                () -> assertFalse(cmd.flag("any"))
            );
        }

        @Test
        @DisplayName("Should parse options and flags while ignoring positional arguments")
        void shouldParseOptionsAndFlagsWhileIgnoringPositionalArguments() {
            String[] args = {"run", "-key", "value", "--verbose", "job"};
            var cmd = CommandLine.parse(args);
            assertAll(
                () -> assertEquals("value", cmd.option("key").orElseThrow()),
                () -> assertTrue(cmd.flag("verbose")),
                () -> assertTrue(cmd.option("run").isEmpty()),
                () -> assertFalse(cmd.flag("run")),
                () -> assertTrue(cmd.option("job").isEmpty()),
                () -> assertFalse(cmd.flag("job"))
            );
        }
    }
}
