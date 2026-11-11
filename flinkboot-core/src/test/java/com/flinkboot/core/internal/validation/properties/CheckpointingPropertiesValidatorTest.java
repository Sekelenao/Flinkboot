package com.flinkboot.core.internal.validation.properties;

import com.flinkboot.core.api.properties.checkpointing.CheckpointStorageType;
import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
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
import org.mockito.Answers;
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

        @ParameterizedTest
        @NullSource
        @ValueSource(booleans = {true})
        @DisplayName("Should return false and register violation when checkpointing is enabled or default without an interval")
        void shouldFailWhenCheckpointingIsEnabledWithoutInterval(Boolean enabled) {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new CheckpointingProperties(
                enabled, null, null, null, null, null, null, null, null, null, null, null
            );

            assertFalse(CheckpointingPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should return true when checkpointing is disabled without an interval")
        void shouldPassWhenCheckpointingIsDisabledWithoutInterval() {
            var context = mock(ConstraintValidatorContext.class);

            var props = new CheckpointingProperties(
                false, null, null, null, null, null, null, null, null, null, null, null
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
                null,
                null,
                null
            );

            assertTrue(CheckpointingPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should return false when min pause is positive and max concurrent checkpoints exceeds one")
        void shouldFailWhenMinPauseIsPositiveAndMaxConcurrentExceedsOne() {
            var context = mock(
                ConstraintValidatorContext.class,
                Answers.RETURNS_DEEP_STUBS
            );

            var props = new CheckpointingProperties(
                true,
                Duration.ofSeconds(10),
                null,
                null,
                Duration.ofSeconds(5),
                2,
                null,
                null,
                null,
                null,
                null,
                null
            );

            assertFalse(
                CheckpointingPropertiesValidator.validate(props, context)
            );
        }

        @Test
        @DisplayName("Should pass when min pause is positive and max concurrent checkpoints is one")
        void shouldPassWhenMinPauseIsPositiveAndMaxConcurrentIsOne() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new CheckpointingProperties(
                true, Duration.ofSeconds(10), null, null, Duration.ofSeconds(5), 1,
                null, null, null, null, null, null
            );

            assertTrue(CheckpointingPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when min pause is zero and max concurrent checkpoints exceeds one")
        void shouldPassWhenMinPauseIsZeroAndMaxConcurrentExceedsOne() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new CheckpointingProperties(
                true, Duration.ofSeconds(10), null, null, Duration.ZERO, 2,
                null, null, null, null, null, null
            );

            assertTrue(CheckpointingPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when only min pause is specified")
        void shouldPassWhenOnlyMinPauseIsPresent() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new CheckpointingProperties(
                true, Duration.ofSeconds(10), null, null, Duration.ofSeconds(5), null,
                null, null, null, null, null, null
            );

            assertTrue(CheckpointingPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when only max concurrent checkpoints is specified")
        void shouldPassWhenOnlyMaxConcurrentIsPresent() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new CheckpointingProperties(
                true, Duration.ofSeconds(10), null, null, null, 2,
                null, null, null, null, null, null
            );

            assertTrue(CheckpointingPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when storage is FILESYSTEM and storage-uri is null")
        void shouldFailWhenStorageIsFileSystemAndStorageUriIsNull() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new CheckpointingProperties(
                true, Duration.ofSeconds(10), null, null, null, null,
                null, null, null, CheckpointStorageType.FILESYSTEM, null, null
            );

            assertFalse(CheckpointingPropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "storage-uri must be specified when checkpoint storage is FILESYSTEM"
            )).addPropertyNode("storage-uri");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Should fail when storage is FILESYSTEM and storage-uri is blank")
        void shouldFailWhenStorageIsFileSystemAndStorageUriIsBlank(String blankUri) {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new CheckpointingProperties(
                true, Duration.ofSeconds(10), null, null, null, null,
                null, null, null, CheckpointStorageType.FILESYSTEM, blankUri, null
            );

            assertFalse(CheckpointingPropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "storage-uri must be specified when checkpoint storage is FILESYSTEM"
            )).addPropertyNode("storage-uri");
        }

        @Test
        @DisplayName("Should pass when storage is FILESYSTEM and storage-uri is provided")
        void shouldPassWhenStorageIsFileSystemAndStorageUriIsProvided() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new CheckpointingProperties(
                true, Duration.ofSeconds(10), null, null, null, null,
                null, null, null, CheckpointStorageType.FILESYSTEM, "file:///tmp/checkpoints", null
            );

            assertTrue(CheckpointingPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when storage is JOBMANAGER without storage-uri")
        void shouldPassWhenStorageIsJobManagerWithoutStorageUri() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new CheckpointingProperties(
                true, Duration.ofSeconds(10), null, null, null, null,
                null, null, null, CheckpointStorageType.JOBMANAGER, null, null
            );

            assertTrue(CheckpointingPropertiesValidator.validate(props, context));
        }
    }
}
