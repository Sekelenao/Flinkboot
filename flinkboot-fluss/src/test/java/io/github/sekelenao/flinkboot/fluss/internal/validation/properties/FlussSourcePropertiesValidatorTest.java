package io.github.sekelenao.flinkboot.fluss.internal.validation.properties;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;
import static org.mockito.Mockito.mock;

import io.github.sekelenao.flinkboot.fluss.api.properties.source.FlussSourceProperties;
import io.github.sekelenao.flinkboot.fluss.api.properties.source.FlussStartupMode;
import jakarta.validation.ConstraintValidatorContext;

@DisplayName("FlussSourcePropertiesValidator")
class FlussSourcePropertiesValidatorTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw AssertionError when trying to instantiate via reflection")
        void shouldThrowWhenInstantiatedViaReflection() throws Exception {
            var constructor = FlussSourcePropertiesValidator.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            var targetException = assertThrows(InvocationTargetException.class, constructor::newInstance);
            assertInstanceOf(AssertionError.class, targetException.getCause());
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should throw NullPointerException when properties is null")
        void shouldThrowWhenPropertiesIsNull() {
            var context = mock(ConstraintValidatorContext.class);

            var exception = assertThrows(
                NullPointerException.class,
                () -> FlussSourcePropertiesValidator.validate(null, context)
            );

            assertEquals("properties must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when context is null")
        void shouldThrowWhenContextIsNull() {
            var props = new FlussSourceProperties(
                "fluss-source",
                List.of("localhost:9123"),
                "db",
                "table",
                null,
                null,
                Map.of()
            );

            var exception = assertThrows(
                NullPointerException.class,
                () -> FlussSourcePropertiesValidator.validate(props, null)
            );

            assertEquals("context must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should pass when startupMode is null")
        void shouldPassWhenStartupModeIsNull() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new FlussSourceProperties(
                "fluss-source",
                List.of("localhost:9123"),
                "db",
                "table",
                null,
                null,
                Map.of()
            );

            assertTrue(FlussSourcePropertiesValidator.validate(props, context));
        }

        @ParameterizedTest
        @EnumSource(value = FlussStartupMode.class, names = "TIMESTAMP", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should pass when startupMode is not TIMESTAMP and startupTimestamp is null")
        void shouldPassWhenNonTimestampModeAndTimestampIsNull(FlussStartupMode mode) {
            var context = mock(ConstraintValidatorContext.class);
            var props = new FlussSourceProperties(
                "fluss-source",
                List.of("localhost:9123"),
                "db",
                "table",
                mode,
                null,
                Map.of()
            );

            assertTrue(FlussSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when startupMode is TIMESTAMP and startupTimestamp is provided")
        void shouldPassWhenTimestampModeAndTimestampIsProvided() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new FlussSourceProperties(
                "fluss-source",
                List.of("localhost:9123"),
                "db",
                "table",
                FlussStartupMode.TIMESTAMP,
                123456789L,
                Map.of()
            );

            assertTrue(FlussSourcePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when startupMode is TIMESTAMP and startupTimestamp is null")
        void shouldFailWhenTimestampModeAndTimestampIsNull() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

            var props = new FlussSourceProperties(
                "fluss-source",
                List.of("localhost:9123"),
                "db",
                "table",
                FlussStartupMode.TIMESTAMP,
                null,
                Map.of()
            );

            assertFalse(FlussSourcePropertiesValidator.validate(props, context));
        }

        @ParameterizedTest
        @EnumSource(value = FlussStartupMode.class, names = "TIMESTAMP", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should fail when startupMode is not TIMESTAMP and startupTimestamp is provided")
        void shouldFailWhenNonTimestampModeAndTimestampIsProvided(FlussStartupMode mode) {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);

            var props = new FlussSourceProperties(
                "fluss-source",
                List.of("localhost:9123"),
                "db",
                "table",
                mode,
                123456789L,
                Map.of()
            );

            assertFalse(FlussSourcePropertiesValidator.validate(props, context));
        }
    }
}
