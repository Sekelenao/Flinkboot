package com.flinkboot.core.internal.validation.properties;

import com.flinkboot.core.api.validation.ValidatableProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("PropertiesValidator")
class PropertiesValidatorTest {

    @Nested
    @DisplayName("isValid")
    class IsValid {

        @Test
        @DisplayName("Should return true when value is null and context is present")
        void shouldReturnTrueWhenValueIsNull() {
            var validator = new PropertiesValidator();
            assertTrue(validator.isValid(null, mock(ConstraintValidatorContext.class)));
        }

        @Test
        @DisplayName("Should throw NullPointerException when context is null")
        void shouldThrowWhenContextIsNull() {
            var validator = new PropertiesValidator();
            assertThrows(NullPointerException.class, () -> validator.isValid(null, null));
        }

        @Test
        @DisplayName("Should delegate to value.validate() when value is non-null")
        void shouldDelegateToValidate() {
            var validator = new PropertiesValidator();
            var context = mock(ConstraintValidatorContext.class);

            ValidatableProperties validMock = ctx -> true;
            ValidatableProperties invalidMock = ctx -> false;

            assertTrue(validator.isValid(validMock, context));
            assertFalse(validator.isValid(invalidMock, context));
        }
    }

    @Nested
    @DisplayName("compute")
    class Compute {

        @Test
        @DisplayName("Should throw NullPointerException when arguments are null")
        void shouldThrowWhenArgumentsAreNull() {
            var context = mock(ConstraintValidatorContext.class);
            List<ValidationRule<String>> rules = List.of();
            List<ValidationRule<String>> rulesWithNull = Collections.singletonList(null);

            assertThrows(NullPointerException.class, () -> PropertiesValidator.compute(null, context, rules));
            assertThrows(NullPointerException.class, () -> PropertiesValidator.compute("props", null, rules));
            assertThrows(NullPointerException.class, () -> PropertiesValidator.compute("props", context, null));
            assertThrows(NullPointerException.class, () -> PropertiesValidator.compute("props", context, rulesWithNull));
        }

        @Test
        @DisplayName("Should return true when all rules pass")
        void shouldReturnTrueWhenAllRulesPass() {
            var context = mock(ConstraintValidatorContext.class);
            List<ValidationRule<String>> rules = List.of(p -> Optional.empty(), p -> Optional.empty());

            var valid = PropertiesValidator.compute("props", context, rules);

            assertTrue(valid);
            verify(context, never()).disableDefaultConstraintViolation();
        }

        @Test
        @DisplayName("Should evaluate all rules without short-circuiting and register all violations")
        void shouldAccumulateAllViolations() {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate("first error")).thenReturn(builder);
            when(context.buildConstraintViolationWithTemplate("second error")).thenReturn(builder);
            when(builder.addPropertyNode("first")).thenReturn(nodeBuilder);
            when(builder.addPropertyNode("second")).thenReturn(nodeBuilder);

            List<ValidationRule<String>> rules = List.of(
                p -> Optional.of(new PropertyViolation("first", "first error")),
                p -> Optional.empty(),
                p -> Optional.of(new PropertyViolation("second", "second error"))
            );

            var valid = PropertiesValidator.compute("props", context, rules);

            assertFalse(valid);
            verify(context).buildConstraintViolationWithTemplate("first error");
            verify(builder).addPropertyNode("first");
            verify(context).buildConstraintViolationWithTemplate("second error");
            verify(builder).addPropertyNode("second");
        }
    }
}
