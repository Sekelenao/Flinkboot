package com.flinkboot.core.internal.validation.properties.state;

import com.flinkboot.core.api.properties.state.StateBackendProperties;
import com.flinkboot.core.api.properties.state.StateBackendType;
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
        @DisplayName("Should return true when all state backend rules pass")
        void shouldPassWhenAllRulesPass() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new StateBackendProperties(StateBackendType.ROCKSDB, true, false, null, null);

            assertTrue(StateBackendPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should accumulate all violations when multiple state backend rules fail simultaneously")
        void shouldAccumulateAllViolations() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new StateBackendProperties(StateBackendType.HASHMAP, true, null, "com.example.MyFactory", null);

            assertFalse(StateBackendPropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "custom-class can only be specified when state backend type is CUSTOM"
            )).addPropertyNode("custom-class");
            verify(context.buildConstraintViolationWithTemplate(
                "incremental cannot be true when state backend type is HASHMAP"
            )).addPropertyNode("incremental");
        }
    }
}
