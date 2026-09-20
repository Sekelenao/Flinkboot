package io.github.sekelenao.flinkboot.test.internal.pojo.handler;

import io.github.sekelenao.flinkboot.test.internal.pojo.PojoValidationTask;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
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

@DisplayName("CompositeTypeHandler")
class CompositeTypeHandlerTest {

    private CompositeTypeHandler handler;
    private TupleTypeInfo<?> tupleTypeInfo;

    @BeforeEach
    void setUp() {
        handler = new CompositeTypeHandler();
        tupleTypeInfo = new TupleTypeInfo<>(BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO);
    }

    @Nested
    @DisplayName("supports")
    class Supports {

        @Test
        @DisplayName("Should return true when type info is CompositeType")
        void shouldReturnTrueWhenCompositeType() {
            assertTrue(handler.supports(tupleTypeInfo));
        }

        @Test
        @DisplayName("Should return false when type info is not CompositeType")
        void shouldReturnFalseWhenNotCompositeType() {
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
        @DisplayName("Should enqueue positional field tasks with indexed bracket suffixes")
        void shouldEnqueuePositionalFieldTasks() {
            var task = new PojoValidationTask<>("tuple", tupleTypeInfo);
            var enqueued = new ArrayList<PojoValidationTask<?>>();

            handler.handle(task, enqueued::add);

            assertAll(
                () -> assertEquals(2, enqueued.size()),
                () -> assertEquals("tuple[0]", enqueued.get(0).path()),
                () -> assertEquals(BasicTypeInfo.STRING_TYPE_INFO, enqueued.get(0).typeInfo()),
                () -> assertEquals("tuple[1]", enqueued.get(1).path()),
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
            var task = new PojoValidationTask<>("tuple", tupleTypeInfo);
            var ex = assertThrows(NullPointerException.class, () -> handler.handle(task, null));
            assertEquals("enqueuer must not be null", ex.getMessage());
        }

    }

}
