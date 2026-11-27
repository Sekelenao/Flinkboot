package com.flinkboot.fluss.internal.validation.properties.source;

import com.flinkboot.fluss.api.properties.source.FlussSourceProperties;
import com.flinkboot.fluss.api.properties.source.FlussStartupMode;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        @DisplayName("Should return true when all Fluss source rules pass")
        void shouldPassWhenAllRulesPass() {
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
        @DisplayName("Should return false and register violation when Fluss source rule fails")
        void shouldFailWhenStartupTimestampRuleFails() {
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
            verify(context.buildConstraintViolationWithTemplate(
                "startup-timestamp is required when startup-mode is TIMESTAMP"
            )).addPropertyNode("startup-timestamp");
        }
    }
}
