package io.github.sekelenao.flinkboot.test.internal.pojo.handler;

import io.github.sekelenao.flinkboot.test.internal.pojo.PojoValidationTask;
import org.apache.flink.api.common.functions.InvalidTypesException;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.java.typeutils.MissingTypeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MissingTypeHandler")
class MissingTypeHandlerTest {

    private MissingTypeHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MissingTypeHandler();
    }

    @Nested
    @DisplayName("supports")
    class Supports {

        @Test
        @DisplayName("Should return true when type info is MissingTypeInfo")
        void shouldReturnTrueWhenMissingTypeInfo() {
            assertTrue(handler.supports(new MissingTypeInfo("func")));
        }

        @Test
        @DisplayName("Should return false when type info is not MissingTypeInfo")
        void shouldReturnFalseWhenNotMissingTypeInfo() {
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
        @DisplayName("Should fail assertion with reason from type exception")
        void shouldFailWithExceptionMessageWhenTypeExceptionPresent() {
            var task = new PojoValidationTask<>(
                "root.field",
                new MissingTypeInfo("func", new InvalidTypesException("Cannot determine type."))
            );
            var error = assertThrows(AssertionFailedError.class, () -> handler.handle(task, ignored -> {}));
            assertEquals(
                "Field or type 'root.field' has missing type information (Cannot determine type.) and cannot be serialized by Flink.",
                error.getMessage()
            );
        }

        @Test
        @DisplayName("Should fail assertion with fallback message when type exception is absent")
        void shouldFailWithFallbackMessageWhenTypeExceptionAbsent() {
            var task = new PojoValidationTask<>("root.field", new MissingTypeInfo("func", null));
            var error = assertThrows(AssertionFailedError.class, () -> handler.handle(task, ignored -> {}));
            assertEquals(
                "Field or type 'root.field' has missing type information (unknown type erasure) and cannot be serialized by Flink.",
                error.getMessage()
            );
        }

        @Test
        @DisplayName("Should fail assertion with fallback message when type exception has null message")
        void shouldFailWithFallbackMessageWhenTypeExceptionHasNullMessage() {
            var task = new PojoValidationTask<>(
                "root.field",
                new MissingTypeInfo("func", new InvalidTypesException((String) null))
            );
            var error = assertThrows(AssertionFailedError.class, () -> handler.handle(task, ignored -> {}));
            assertEquals(
                "Field or type 'root.field' has missing type information (unknown type erasure) and cannot be serialized by Flink.",
                error.getMessage()
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
            var task = new PojoValidationTask<>("root", new MissingTypeInfo("func"));
            var ex = assertThrows(NullPointerException.class, () -> handler.handle(task, null));
            assertEquals("enqueuer must not be null", ex.getMessage());
        }

    }

}
