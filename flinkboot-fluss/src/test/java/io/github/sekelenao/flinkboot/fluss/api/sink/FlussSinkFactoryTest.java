package io.github.sekelenao.flinkboot.fluss.api.sink;

import io.github.sekelenao.flinkboot.fluss.api.properties.sink.FlussSinkProperties;
import org.apache.fluss.flink.row.RowWithOp;
import org.apache.fluss.flink.sink.serializer.FlussSerializationSchema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("FlussSinkFactory")
class FlussSinkFactoryTest {

    private static final FlussSerializationSchema<String> TEST_SCHEMA = new FlussSerializationSchema<>() {
        private static final long serialVersionUID = 1L;

        @Override
        public void open(InitializationContext context) { /* Do nothing */ }

        @Override
        public RowWithOp serialize(String element) { return null; }
    };

    @Test
    @DisplayName("Private constructor should throw AssertionError")
    void testConstructorIsPrivate() throws Exception {
        var constructor = FlussSinkFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
    }

    @Nested
    @DisplayName("supplyFor & supplyBuilderFor")
    class SupplyTests {

        @Test
        @DisplayName("Should successfully create FlussSinkBuilder with all options")
        void shouldCreateBuilderWithAllOptions() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                1048576L,
                50L,
                Map.of("custom.key", "custom.value")
            );

            var builder = FlussSinkFactory.supplyBuilderFor(props, TEST_SCHEMA);
            assertNotNull(builder);

            assertThrows(RuntimeException.class, () -> FlussSinkFactory.supplyFor(props, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should successfully create FlussSinkBuilder with minimal options")
        void shouldCreateBuilderWithMinimalOptions() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                null,
                null,
                null
            );

            var builder = FlussSinkFactory.supplyBuilderFor(props, TEST_SCHEMA);
            assertNotNull(builder);

            assertThrows(RuntimeException.class, () -> FlussSinkFactory.supplyFor(props, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should throw NullPointerException when parameters are null")
        void shouldThrowNpeWhenParametersAreNull() {
            var props = new FlussSinkProperties(
                "my-sink",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                null,
                null,
                null
            );

            assertAll(
                () -> assertThrows(NullPointerException.class, () -> FlussSinkFactory.supplyBuilderFor(null, TEST_SCHEMA)),
                () -> assertThrows(NullPointerException.class, () -> FlussSinkFactory.supplyBuilderFor(props, null)),
                () -> assertThrows(NullPointerException.class, () -> FlussSinkFactory.supplyFor(null, TEST_SCHEMA)),
                () -> assertThrows(NullPointerException.class, () -> FlussSinkFactory.supplyFor(props, null))
            );
        }
    }
}
