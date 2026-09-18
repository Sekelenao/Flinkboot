package io.github.sekelenao.flinkboot.core.internal.validation.properties;

import java.lang.reflect.InvocationTargetException;

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
import org.junit.jupiter.params.provider.NullSource;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.sekelenao.flinkboot.core.api.properties.state.CheckpointStorageType;
import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendProperties;
import io.github.sekelenao.flinkboot.core.api.properties.state.StateBackendType;
import jakarta.validation.ConstraintValidatorContext;

@DisplayName("StateBackendPropertiesValidator")
class StateBackendPropertiesValidatorTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw AssertionError when trying to instantiate via reflection")
        void shouldThrowWhenInstantiatedViaReflection() throws Exception {
            var constructor = StateBackendPropertiesValidator.class.getDeclaredConstructor();
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
                () -> StateBackendPropertiesValidator.validate(null, context)
            );

            assertEquals("properties must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when context is null")
        void shouldThrowWhenContextIsNull() {
            var props = new StateBackendProperties(StateBackendType.ROCKSDB, CheckpointStorageType.FILESYSTEM, true, false, "");

            var exception = assertThrows(
                NullPointerException.class,
                () -> StateBackendPropertiesValidator.validate(props, null)
            );

            assertEquals("context must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should pass when state backend is null and customClass is null")
        void shouldPassWhenStateBackendAndCustomClassNull() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new StateBackendProperties(null, null, null, null, null);

            assertTrue(StateBackendPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass when state backend is non-CUSTOM and customClass is null")
        void shouldPassWhenNonCustomAndCustomClassNull() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new StateBackendProperties(StateBackendType.HASHMAP, CheckpointStorageType.JOBMANAGER, false, true, null);

            assertTrue(StateBackendPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when state backend is non-CUSTOM and customClass is empty string")
        void shouldFailWhenNonCustomAndCustomClassEmpty() {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("customClass")).thenReturn(nodeBuilder);

            var props = new StateBackendProperties(StateBackendType.ROCKSDB, CheckpointStorageType.FILESYSTEM, true, false, "");

            assertFalse(StateBackendPropertiesValidator.validate(props, context));
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("custom-class can only be specified when state backend type is CUSTOM");
            verify(builder).addPropertyNode("customClass");
            verify(nodeBuilder).addConstraintViolation();
        }

        @Test
        @DisplayName("Should pass when state backend is CUSTOM and customClass is non-blank")
        void shouldPassWhenCustomAndCustomClassProvided() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new StateBackendProperties(StateBackendType.CUSTOM, CheckpointStorageType.FILESYSTEM, false, false, "com.example.MyFactory");

            assertTrue(StateBackendPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should pass cross-field validation when state backend is CUSTOM and customClass is empty string")
        void shouldPassWhenCustomAndCustomClassIsEmptyString() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new StateBackendProperties(StateBackendType.CUSTOM, CheckpointStorageType.FILESYSTEM, false, false, "");

            assertTrue(StateBackendPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when state backend is CUSTOM and customClass is null")
        void shouldFailWhenCustomAndCustomClassNull() {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("customClass")).thenReturn(nodeBuilder);

            var props = new StateBackendProperties(StateBackendType.CUSTOM, null, null, null, null);

            assertFalse(StateBackendPropertiesValidator.validate(props, context));
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("custom-class must be specified when state backend type is CUSTOM");
            verify(builder).addPropertyNode("customClass");
            verify(nodeBuilder).addConstraintViolation();
        }

        @ParameterizedTest
        @NullSource
        @EnumSource(value = StateBackendType.class, names = "CUSTOM", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should fail when state backend is not CUSTOM and customClass is provided")
        void shouldFailWhenNotCustomAndCustomClassProvided(StateBackendType type) {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("customClass")).thenReturn(nodeBuilder);

            var props = new StateBackendProperties(type, null, null, null, "com.example.MyFactory");

            assertFalse(StateBackendPropertiesValidator.validate(props, context));
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("custom-class can only be specified when state backend type is CUSTOM");
            verify(builder).addPropertyNode("customClass");
            verify(nodeBuilder).addConstraintViolation();
        }
    }
}
