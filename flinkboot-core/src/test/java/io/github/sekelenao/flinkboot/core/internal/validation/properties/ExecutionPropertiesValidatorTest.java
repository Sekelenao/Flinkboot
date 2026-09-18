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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.sekelenao.flinkboot.core.api.properties.execution.ExecutionProperties;
import jakarta.validation.ConstraintValidatorContext;

@DisplayName("ExecutionPropertiesValidator")
class ExecutionPropertiesValidatorTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw AssertionError when trying to instantiate via reflection")
        void shouldThrowWhenInstantiatedViaReflection() throws Exception {
            var constructor = ExecutionPropertiesValidator.class.getDeclaredConstructor();
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
                () -> ExecutionPropertiesValidator.validate(null, context)
            );

            assertEquals("properties must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when context is null")
        void shouldThrowWhenContextIsNull() {
            var props = new ExecutionProperties(null, 4, 16, null, null, null);

            var exception = assertThrows(
                NullPointerException.class,
                () -> ExecutionPropertiesValidator.validate(props, null)
            );

            assertEquals("context must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should return true when parallelism and maxParallelism are null")
        void shouldPassWhenFieldsAreNull() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new ExecutionProperties(null, null, null, null, null, null);

            assertTrue(ExecutionPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should return true when only parallelism is provided")
        void shouldPassWhenOnlyParallelismProvided() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new ExecutionProperties(null, 4, null, null, null, null);

            assertTrue(ExecutionPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should return true when only maxParallelism is provided")
        void shouldPassWhenOnlyMaxParallelismProvided() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new ExecutionProperties(null, null, 16, null, null, null);

            assertTrue(ExecutionPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should return true when parallelism is less than or equal to maxParallelism")
        void shouldPassWhenParallelismIsLessOrEqual() {
            var context = mock(ConstraintValidatorContext.class);
            var props1 = new ExecutionProperties(null, 4, 16, null, null, null);
            var props2 = new ExecutionProperties(null, 16, 16, null, null, null);

            assertTrue(ExecutionPropertiesValidator.validate(props1, context));
            assertTrue(ExecutionPropertiesValidator.validate(props2, context));
        }

        @Test
        @DisplayName("Should return false and register violation when parallelism exceeds maxParallelism")
        void shouldFailWhenParallelismExceedsMaxParallelism() {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("parallelism")).thenReturn(nodeBuilder);

            var props = new ExecutionProperties(null, 32, 16, null, null, null);

            assertFalse(ExecutionPropertiesValidator.validate(props, context));
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("parallelism (32) cannot exceed max-parallelism (16)");
            verify(builder).addPropertyNode("parallelism");
            verify(nodeBuilder).addConstraintViolation();
        }
    }
}
