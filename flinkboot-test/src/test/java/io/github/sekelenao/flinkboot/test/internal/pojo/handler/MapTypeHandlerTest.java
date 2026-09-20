package io.github.sekelenao.flinkboot.test.internal.pojo.handler;

import io.github.sekelenao.flinkboot.test.internal.pojo.PojoValidationTask;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.typeutils.MapTypeInfo;
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

@DisplayName("MapTypeHandler")
class MapTypeHandlerTest {

    private MapTypeHandler handler;
    private MapTypeInfo<String, Integer> mapTypeInfo;

    @BeforeEach
    void setUp() {
        handler = new MapTypeHandler();
        mapTypeInfo = new MapTypeInfo<>(BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO);
    }

    @Nested
    @DisplayName("supports")
    class Supports {

        @Test
        @DisplayName("Should return true when type info is MapTypeInfo")
        void shouldReturnTrueWhenMapTypeInfo() {
            assertTrue(handler.supports(mapTypeInfo));
        }

        @Test
        @DisplayName("Should return false when type info is not MapTypeInfo")
        void shouldReturnFalseWhenNotMapTypeInfo() {
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
        @DisplayName("Should enqueue key and value tasks with <key> and <value> suffixes")
        void shouldEnqueueKeyAndValueTasks() {
            var task = new PojoValidationTask<>("config", mapTypeInfo);
            var enqueued = new ArrayList<PojoValidationTask<?>>();

            handler.handle(task, enqueued::add);

            assertAll(
                () -> assertEquals(2, enqueued.size()),
                () -> assertEquals("config<key>", enqueued.get(0).path()),
                () -> assertEquals(BasicTypeInfo.STRING_TYPE_INFO, enqueued.get(0).typeInfo()),
                () -> assertEquals("config<value>", enqueued.get(1).path()),
                () -> assertEquals(BasicTypeInfo.INT_TYPE_INFO, enqueued.get(1).typeInfo())
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
            var task = new PojoValidationTask<>("config", mapTypeInfo);
            var ex = assertThrows(NullPointerException.class, () -> handler.handle(task, null));
            assertEquals("enqueuer must not be null", ex.getMessage());
        }

    }

}
