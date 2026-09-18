package io.github.sekelenao.flinkboot.core.internal.validation.properties;

import io.github.sekelenao.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

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
        @DisplayName("Should return false and register violation when checkpointing is enabled without an interval")
        void shouldFailWhenCheckpointingIsEnabledWithoutInterval() {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("interval")).thenReturn(nodeBuilder);

            var props = new CheckpointingProperties(
                true, null, null, null, null, null, null, null, null, null
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
                java.time.Duration.ofSeconds(10),
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