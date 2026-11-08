package com.flinkboot.test.internal.pojo.handler;

import com.flinkboot.test.internal.pojo.PojoValidationTask;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.typeutils.GenericTypeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
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
        @DisplayName("Should record error reporting GenericTypeInfo and Kryo fallback")
        void shouldRecordErrorWhenGenericTypeInfo() {
            var task = new PojoValidationTask<>(
                "root.unsupportedField",
                new GenericTypeInfo<>(OffsetDateTime.class)
            );
            var errors = new ArrayList<String>();
            var enqueued = new ArrayList<PojoValidationTask<?>>();
            handler.handle(task, enqueued::add, errors::add);
            assertAll(
                () -> assertTrue(enqueued.isEmpty()),
                () -> assertEquals(
                    List.of("Field or type 'root.unsupportedField' is recognized as GenericTypeInfo (java.time.OffsetDateTime), which falls back to Kryo serialization."),
                    errors
                )
            );
        }

        @Test
        @DisplayName("Should throw NullPointerException when task is null")
        void shouldThrowWhenTaskIsNull() {
            var ex = assertThrows(NullPointerException.class, () -> handler.handle(null, ignored -> {}, ignored -> {}));
            assertEquals("task must not be null", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when enqueuer is null")
        void shouldThrowWhenEnqueuerIsNull() {
            var task = new PojoValidationTask<>("root", new GenericTypeInfo<>(OffsetDateTime.class));
            var ex = assertThrows(NullPointerException.class, () -> handler.handle(task, null, ignored -> {}));
            assertEquals("enqueuer must not be null", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when errors is null")
        void shouldThrowWhenErrorsIsNull() {
            var task = new PojoValidationTask<>("root", new GenericTypeInfo<>(OffsetDateTime.class));
            var ex = assertThrows(NullPointerException.class, () -> handler.handle(task, ignored -> {}, null));
            assertEquals("errors must not be null", ex.getMessage());
        }

    }

}
