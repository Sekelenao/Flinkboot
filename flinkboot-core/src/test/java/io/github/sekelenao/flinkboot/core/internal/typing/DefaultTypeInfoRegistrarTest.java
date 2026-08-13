package io.github.sekelenao.flinkboot.core.internal.typing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DefaultTypeInfoRegistrar")
class DefaultTypeInfoRegistrarTest {

    @Test
    @DisplayName("Should have private constructor that throws AssertionError to prevent instantiation")
    void shouldPreventInstantiation() throws Exception {
        var constructor = DefaultTypeInfoRegistrar.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()), "Constructor should be private");
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("You cannot instantiate this class"));
    }

    @Nested
    @DisplayName("Register Default Factories")
    class RegisterDefaultFactories {

        @Test
        @DisplayName("Should register all default type info factories without throwing")
        void shouldRegisterDefaultFactories() {
            assertDoesNotThrow(DefaultTypeInfoRegistrar::registerDefaultFactories);
        }

    }

}
