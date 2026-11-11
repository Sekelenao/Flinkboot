package com.flinkboot.core.internal.validation.properties;

import com.flinkboot.core.api.properties.savepoint.RestoreMode;
import com.flinkboot.core.api.properties.savepoint.SavepointRestoreProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        @DisplayName("Should throw NullPointerException when properties is null")
        void shouldThrowWhenPropertiesIsNull() {
            var context = mock(ConstraintValidatorContext.class);

            var exception = assertThrows(
                NullPointerException.class,
                () -> SavepointRestorePropertiesValidator.validate(null, context)
            );

            assertEquals("properties must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when context is null")
        void shouldThrowWhenContextIsNull() {
            var props = new SavepointRestoreProperties("/tmp/savepoint", null, null, null);

            var exception = assertThrows(
                NullPointerException.class,
                () -> SavepointRestorePropertiesValidator.validate(props, null)
            );

            assertEquals("context must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should fail when both savepointPath and directory are empty")
        void shouldFailWhenBothSavepointPathAndDirectoryAreEmpty() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new SavepointRestoreProperties(null, true, RestoreMode.CLAIM, null);

            assertFalse(SavepointRestorePropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "at least one of savepoint-path or directory must be specified"
            )).addPropertyNode("savepoint-path");
        }

        @Test
        @DisplayName("Should pass when only savepointPath is specified")
        void shouldPassWhenOnlySavepointPathIsSpecified() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new SavepointRestoreProperties("/tmp/savepoint", null, null, null);

            assertTrue(SavepointRestorePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when only directory is specified")
        void shouldPassWhenOnlyDirectoryIsSpecified() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new SavepointRestoreProperties(null, null, null, "/tmp/savepoints");

            assertTrue(SavepointRestorePropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when both savepointPath and directory are specified")
        void shouldPassWhenBothSavepointPathAndDirectoryAreSpecified() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new SavepointRestoreProperties("/tmp/savepoint", true, RestoreMode.CLAIM, "/tmp/savepoints");

            assertTrue(SavepointRestorePropertiesValidator.validate(props, context));
        }
    }
}
