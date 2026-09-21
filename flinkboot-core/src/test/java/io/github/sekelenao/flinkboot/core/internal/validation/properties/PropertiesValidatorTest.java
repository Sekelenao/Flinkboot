package io.github.sekelenao.flinkboot.core.internal.validation.properties;

import io.github.sekelenao.flinkboot.core.api.validation.ValidatableProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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
    @DisplayName("reject")
    class Reject {

        @Test
        @DisplayName("Should throw NullPointerException when context is null")
        void shouldThrowWhenContextIsNull() {
            assertThrows(NullPointerException.class, () -> PropertiesValidator.reject(null, "prop", "msg"));
        }

        @Test
        @DisplayName("Should throw NullPointerException when property is null")
        void shouldThrowWhenPropertyIsNull() {
            var context = mock(ConstraintValidatorContext.class);
            assertThrows(NullPointerException.class, () -> PropertiesValidator.reject(context, null, "msg"));
        }

        @Test
        @DisplayName("Should throw NullPointerException when message is null")
        void shouldThrowWhenMessageIsNull() {
            var context = mock(ConstraintValidatorContext.class);
            assertThrows(NullPointerException.class, () -> PropertiesValidator.reject(context, "prop", null));
        }

        @Test
        @DisplayName("Should disable default violation, build property violation, and return false")
        void shouldBuildViolationAndReturnFalse() {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate("error message")).thenReturn(builder);
            when(builder.addPropertyNode("targetProperty")).thenReturn(nodeBuilder);

            var result = PropertiesValidator.reject(context, "targetProperty", "error message");

            assertFalse(result);
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate("error message");
            verify(builder).addPropertyNode("targetProperty");
            verify(nodeBuilder).addConstraintViolation();
        }
    }
}
