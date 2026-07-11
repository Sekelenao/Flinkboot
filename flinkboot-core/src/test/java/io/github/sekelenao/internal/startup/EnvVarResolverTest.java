package io.github.sekelenao.internal.startup;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

@DisplayName("EnvVarResolver")
class EnvVarResolverTest {

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
            var resolver = new EnvVarResolver(System::getenv);
            assertThrows(NullPointerException.class, () -> resolver.get(null));
        }

        @Test
        @DisplayName("Should resolve environment variable with normalization")
        void shouldResolveNormalizedKey() {
            var env = new HashMap<String, String>();
            env.put("FLINKBOOT_CONFIGURATION", "my-value");
            env.put("SIMPLE_VAR", "simple-value");
            
            var resolver = new EnvVarResolver(env::get);
            
            assertAll(
                () -> assertEquals("my-value", resolver.get("flinkboot-configuration").orElseThrow()),
                () -> assertEquals("my-value", resolver.get("flinkboot.configuration").orElseThrow()),
                () -> assertEquals("my-value", resolver.get("FLINKBOOT-CONFIGURATION").orElseThrow()),
                () -> assertEquals("simple-value", resolver.get("simple-var").orElseThrow()),
                () -> assertTrue(resolver.get("non-existent").isEmpty())
            );
        }
    }
}
