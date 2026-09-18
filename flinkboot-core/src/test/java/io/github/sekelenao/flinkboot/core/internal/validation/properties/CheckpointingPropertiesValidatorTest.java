package io.github.sekelenao.flinkboot.core.internal.validation.properties;

import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CheckpointingPropertiesValidator")
class CheckpointingPropertiesValidatorTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw AssertionError when trying to instantiate via reflection")
        void shouldThrowWhenInstantiatedViaReflection() throws Exception {
            var constructor = CheckpointingPropertiesValidator.class.getDeclaredConstructor();
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
                () -> CheckpointingPropertiesValidator.validate(null, context)
            );

            assertEquals("properties must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when context is null")
        void shouldThrowWhenContextIsNull() {
            var props = new CheckpointingProperties(
                true, Duration.ofSeconds(10), null, null, null, null, null, null, null, null
            );

            var exception = assertThrows(
                NullPointerException.class,
                () -> CheckpointingPropertiesValidator.validate(props, null)
            );

            assertEquals("context must not be null", exception.getMessage());
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(booleans = {true})
        @DisplayName("Should return false and register violation when checkpointing is enabled or default without an interval")
        void shouldFailWhenCheckpointingIsEnabledWithoutInterval(Boolean enabled) {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("interval")).thenReturn(nodeBuilder);

            var props = new CheckpointingProperties(
                enabled, null, null, null, null, null, null, null, null, null
            );

            assertFalse(CheckpointingPropertiesValidator.validate(props, context));
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate(
                "interval must be specified when checkpointing is enabled"
            );
            verify(builder).addPropertyNode("interval");
            verify(nodeBuilder).addConstraintViolation();
        }

        @Test
        @DisplayName("Should return true when checkpointing is disabled without an interval")
        void shouldPassWhenCheckpointingIsDisabledWithoutInterval() {
            var context = mock(ConstraintValidatorContext.class);

            var props = new CheckpointingProperties(
                false, null, null, null, null, null, null, null, null, null
            );

            assertTrue(CheckpointingPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should return true when an interval is provided")
        void shouldPassWhenIntervalIsProvided() {
            var context = mock(ConstraintValidatorContext.class);

            var props = new CheckpointingProperties(
                true,
                Duration.ofSeconds(10),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );

            assertTrue(CheckpointingPropertiesValidator.validate(props, context));
        }
    }
}