package io.github.sekelenao.flinkboot.test.internal.pojo.handler;

import io.github.sekelenao.flinkboot.test.internal.pojo.PojoValidationTask;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.typeutils.GenericTypeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GenericTypeHandler")
class GenericTypeHandlerTest {

    private GenericTypeHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GenericTypeHandler();
    }

    @Nested
    @DisplayName("supports")
    class Supports {

        @Test
        @DisplayName("Should return true when type info is GenericTypeInfo")
        void shouldReturnTrueWhenGenericTypeInfo() {
            assertTrue(handler.supports(new GenericTypeInfo<>(OffsetDateTime.class)));
        }

        @Test
        @DisplayName("Should return false when type info is not GenericTypeInfo")
        void shouldReturnFalseWhenNotGenericTypeInfo() {
            assertFalse(handler.supports(BasicTypeInfo.STRING_TYPE_INFO));
        }

        @Test
        @DisplayName("Should throw NullPointerException when type info is null")
        void shouldThrowWhenTypeInfoIsNull() {
            var ex = assertThrows(NullPointerException.class, () -> handler.supports(null));
            assertEquals("typeInfo must not be null", ex.getMessage());
        }

    }

    @Nested
    @DisplayName("isStructural")
    class IsStructural {

        @Test
        @DisplayName("Should return false for non-structural type handler")
        void shouldReturnFalse() {
            assertFalse(handler.isStructural());
        }

    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("Should fail assertion reporting GenericTypeInfo and Kryo fallback")
        void shouldFailAssertionWhenGenericTypeInfo() {
            var task = new PojoValidationTask<>(
                "root.unsupportedField",
                new GenericTypeInfo<>(OffsetDateTime.class)
            );
            var error = assertThrows(AssertionFailedError.class, () -> handler.handle(task, ignored -> {}));
            assertTrue(error.getMessage().contains("Field or type 'root.unsupportedField' is recognized as GenericTypeInfo (java.time.OffsetDateTime), which falls back to Kryo serialization."));
        }

        @Test
        @DisplayName("Should throw NullPointerException when task is null")
        void shouldThrowWhenTaskIsNull() {
            var ex = assertThrows(NullPointerException.class, () -> handler.handle(null, ignored -> {}));
            assertEquals("task must not be null", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when enqueuer is null")
        void shouldThrowWhenEnqueuerIsNull() {
            var task = new PojoValidationTask<>("root", new GenericTypeInfo<>(OffsetDateTime.class));
            var ex = assertThrows(NullPointerException.class, () -> handler.handle(task, null));
            assertEquals("enqueuer must not be null", ex.getMessage());
        }

    }

}
