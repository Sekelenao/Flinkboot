package io.github.sekelenao.flinkboot.core.internal.startup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("EnvVarResolver")
class EnvVarResolverTest {

    static Stream<Arguments> arguments() {
        // <Environment variable>, <Key to query>, <Value of the environment variable>
        return Stream.of(
            Arguments.of("FLINKBOOT_CONFIGURATIONS", "flinkboot-configurations", "job-configuration.yaml"),
            Arguments.of("SIMPLE", "simple", "easy"),
            Arguments.of("DOT_VAR", "dot.var", ".")
        );
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw NullPointerException when getter is null")
        void shouldThrowExceptionWhenGetterIsNull() {
            assertThrows(NullPointerException.class, () -> new EnvVarResolver(null));
        }
    }

    @Nested
    @DisplayName("Get")
    class Get {

        @Test
        @DisplayName("Should throw NullPointerException when key is null")
        void shouldThrowExceptionWhenKeyIsNull() {
            var resolver = new EnvVarResolver(key -> null);
            assertThrows(NullPointerException.class, () -> resolver.get(null));
        }

        @Test
        @DisplayName("Non existent key")
        void shouldReturnEmptyWhenKeyDoesNotExist() {
            var resolver = new EnvVarResolver(key -> null);
            assertTrue(resolver.get("non-existent").isEmpty());
        }

        @ParameterizedTest
        @MethodSource("io.github.sekelenao.flinkboot.core.internal.startup.EnvVarResolverTest#arguments")
        void shouldResolveKeys(String envVar, String key, String value){
            var environment = Map.of(envVar, value);
            var resolver = new EnvVarResolver(environment::get);
            assertEquals(value, resolver.get(key).orElseThrow());
        }

    }
}
