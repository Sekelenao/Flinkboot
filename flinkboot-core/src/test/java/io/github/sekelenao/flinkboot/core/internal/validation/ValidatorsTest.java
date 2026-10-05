package io.github.sekelenao.flinkboot.core.internal.validation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Validators")
class ValidatorsTest {

    public static class SampleConfig {
        @NotBlank
        public String sampleProperty = "";
    }

    public static class NestedConfig {
        @Valid
        @NotNull
        public SampleConfig childConfig = new SampleConfig();
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw AssertionError when trying to instantiate via reflection")
        void shouldThrowWhenInstantiatedViaReflection() throws Exception {
            var constructor = Validators.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            var targetException = assertThrows(InvocationTargetException.class, constructor::newInstance);
            assertInstanceOf(AssertionError.class, targetException.getCause());
        }
    }

    @Nested
    @DisplayName("Factory")
    class Factory {

        @Test
        @DisplayName("Should provide configured ValidatorFactory that resolves property paths to kebab-case")
        void shouldProvideConfiguredValidatorFactory() {
            try (var factory = Validators.factory()) {
                assertNotNull(factory);
                var validator = factory.getValidator();
                assertNotNull(validator);

                var violations = validator.validate(new SampleConfig());
                assertAll(
                    () -> assertEquals(1, violations.size()),
                    () -> assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("sample-property")))
                );
            }
        }

        @Test
        @DisplayName("Should resolve nested property paths to kebab-case")
        void shouldResolveNestedPathsToKebabCase() {
            try (var factory = Validators.factory()) {
                var validator = factory.getValidator();
                var violations = validator.validate(new NestedConfig());

                assertAll(
                    () -> assertEquals(1, violations.size()),
                    () -> assertTrue(violations.stream().anyMatch(v ->
                        v.getPropertyPath().toString().equals("child-config.sample-property")
                    ), "Path must be resolved to child-config.sample-property")
                );
            }
        }
    }
}
