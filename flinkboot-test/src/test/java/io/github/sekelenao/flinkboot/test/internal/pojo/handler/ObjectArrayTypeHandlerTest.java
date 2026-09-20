package io.github.sekelenao.flinkboot.test.internal.pojo.handler;

import io.github.sekelenao.flinkboot.test.internal.pojo.PojoValidationTask;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.typeutils.ObjectArrayTypeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectArrayTypeHandler")
class ObjectArrayTypeHandlerTest {

    private ObjectArrayTypeHandler handler;
    private ObjectArrayTypeInfo<String[], String> arrayTypeInfo;

    @BeforeEach
    void setUp() {
        handler = new ObjectArrayTypeHandler();
        arrayTypeInfo = ObjectArrayTypeInfo.getInfoFor(String[].class, BasicTypeInfo.STRING_TYPE_INFO);
    }

    @Nested
    @DisplayName("supports")
    class Supports {

        @Test
        @DisplayName("Should return true when type info is ObjectArrayTypeInfo")
        void shouldReturnTrueWhenObjectArrayTypeInfo() {
            assertTrue(handler.supports(arrayTypeInfo));
        }

        @Test
        @DisplayName("Should return false when type info is not ObjectArrayTypeInfo")
        void shouldReturnFalseWhenNotObjectArrayTypeInfo() {
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
        @DisplayName("Should return true for structural type handler")
        void shouldReturnTrue() {
            assertTrue(handler.isStructural());
        }

    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("Should enqueue component type task with array bracket suffix")
        void shouldEnqueueComponentTypeTask() {
            var task = new PojoValidationTask<>("elements", arrayTypeInfo);
            var enqueued = new ArrayList<PojoValidationTask<?>>();

            handler.handle(task, enqueued::add);

            assertAll(
                () -> assertEquals(1, enqueued.size()),
                () -> assertEquals("elements[]", enqueued.get(0).path()),
                () -> assertEquals(BasicTypeInfo.STRING_TYPE_INFO, enqueued.get(0).typeInfo())
            );
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
            var task = new PojoValidationTask<>("elements", arrayTypeInfo);
            var ex = assertThrows(NullPointerException.class, () -> handler.handle(task, null));
            assertEquals("enqueuer must not be null", ex.getMessage());
        }

    }

}
