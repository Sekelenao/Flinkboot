package com.flinkboot.core.internal.validation.properties.execution;

import com.flinkboot.core.api.properties.execution.ExecutionProperties;
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
        @DisplayName("Should return true when all execution rules pass")
        void shouldPassWhenAllRulesPass() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new ExecutionProperties(null, 4, 16, null, null, null);

            assertTrue(ExecutionPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should return false and register violation when parallelism exceeds maxParallelism")
        void shouldFailWhenParallelismExceedsMaxParallelism() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new ExecutionProperties(null, 32, 16, null, null, null);

            assertFalse(ExecutionPropertiesValidator.validate(props, context));
            verify(context).buildConstraintViolationWithTemplate(
                "parallelism (32) cannot exceed max-parallelism (16)"
            );
        }
    }
}
