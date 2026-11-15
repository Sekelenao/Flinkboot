package com.flinkboot.core.internal.validation.properties.checkpointing;

import com.flinkboot.core.api.properties.checkpointing.CheckpointStorageType;
import com.flinkboot.core.api.properties.checkpointing.CheckpointingMode;
import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
                true, Duration.ofSeconds(10), null, null, null, null, null, null, null, null, null, null
            );

            var exception = assertThrows(
                NullPointerException.class,
                () -> CheckpointingPropertiesValidator.validate(props, null)
            );

            assertEquals("context must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should return true when all checkpointing rules pass")
        void shouldPassWhenAllRulesPass() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new CheckpointingProperties(
                true, Duration.ofSeconds(10), null, null, Duration.ofSeconds(5), 1,
                null, null, null, CheckpointStorageType.FILESYSTEM, "s3://bucket/checkpoints", null
            );

            assertTrue(CheckpointingPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should accumulate all violations when multiple checkpointing rules fail simultaneously")
        void shouldAccumulateAllViolations() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new CheckpointingProperties(
                true, null, null, null, Duration.ofSeconds(5), 2,
                null, false, Duration.ofSeconds(5), CheckpointStorageType.FILESYSTEM, null, null
            );

            assertFalse(CheckpointingPropertiesValidator.validate(props, context));
            verify(context).buildConstraintViolationWithTemplate("interval must be specified when checkpointing is enabled");
            verify(context).buildConstraintViolationWithTemplate("storage-uri must be specified when checkpoint storage is FILESYSTEM");
            verify(context).buildConstraintViolationWithTemplate("max-concurrent-checkpoints must be 1 when min-pause-between-checkpoints is greater than 0");
            verify(context).buildConstraintViolationWithTemplate("aligned-checkpoint-timeout cannot be set when unaligned-checkpoints is false");
        }

        @Test
        @DisplayName("Should fail when mode is AT_LEAST_ONCE and unaligned-checkpoints is true")
        void shouldFailWhenModeIsAtLeastOnceAndUnalignedEnabled() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new CheckpointingProperties(
                true, Duration.ofSeconds(10), CheckpointingMode.AT_LEAST_ONCE, null, null, null,
                null, true, null, null, null, null
            );

            assertFalse(CheckpointingPropertiesValidator.validate(props, context));
            verify(context).buildConstraintViolationWithTemplate("unaligned-checkpoints cannot be true when mode is AT_LEAST_ONCE");
        }
    }
}
