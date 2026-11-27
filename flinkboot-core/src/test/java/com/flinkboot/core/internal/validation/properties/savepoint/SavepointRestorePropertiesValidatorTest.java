package com.flinkboot.core.internal.validation.properties.savepoint;

import com.flinkboot.core.api.properties.savepoint.RestoreMode;
import com.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("SavepointRestorePropertiesValidator")
class SavepointRestorePropertiesValidatorTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw AssertionError when trying to instantiate via reflection")
        void shouldThrowWhenInstantiatedViaReflection() throws Exception {
            var constructor = SavepointRestorePropertiesValidator.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            var targetException = assertThrows(
                InvocationTargetException.class,
                constructor::newInstance
            );

            assertInstanceOf(AssertionError.class, targetException.getCause());
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should return true when all savepoint restore rules pass")
        void shouldPassWhenAllRulesPass() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new SavepointRestoreProperties("/tmp/savepoint", true, RestoreMode.CLAIM, "/tmp/savepoints");

            assertTrue(SavepointRestorePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should accumulate all violations when multiple savepoint restore rules fail simultaneously")
        void shouldAccumulateAllViolations() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new SavepointRestoreProperties(null, true, RestoreMode.CLAIM, null);

            assertFalse(SavepointRestorePropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "at least one of savepoint-path or directory must be specified"
            )).addPropertyNode("savepoint-path");
            verify(context.buildConstraintViolationWithTemplate(
                "allow-non-restored-state cannot be set when savepoint-path is absent"
            )).addPropertyNode("allow-non-restored-state");
            verify(context.buildConstraintViolationWithTemplate(
                "restore-mode cannot be set when savepoint-path is absent"
            )).addPropertyNode("restore-mode");
        }
    }
}
