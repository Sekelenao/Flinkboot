package io.github.sekelenao.flinkboot.fluss.api.source;

import io.github.sekelenao.flinkboot.fluss.api.properties.source.FlussSourceProperties;
import io.github.sekelenao.flinkboot.fluss.api.properties.source.FlussStartupMode;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.fluss.flink.source.deserializer.FlussDeserializationSchema;
import org.apache.fluss.record.LogRecord;
import org.apache.fluss.types.RowType;
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

@DisplayName("FlussSourceFactory")
class FlussSourceFactoryTest {

    private static final FlussDeserializationSchema<String> TEST_SCHEMA = new FlussDeserializationSchema<>() {
        private static final long serialVersionUID = 1L;

        @Override
        public void open(InitializationContext context) { /* Do nothing */ }

        @Override
        public String deserialize(LogRecord record) { return "test"; }

        @Override
        public TypeInformation<String> getProducedType(RowType rowType) {
            return Types.STRING;
        }
    };

    @Test
    @DisplayName("Private constructor should throw AssertionError")
    void testConstructorIsPrivate() throws Exception {
        var constructor = FlussSourceFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
    }

    @Nested
    @DisplayName("supplyFor & supplyBuilderFor")
    class SupplyTests {

        @Test
        @DisplayName("Should successfully create FlussSourceBuilder with EARLIEST mode")
        void shouldCreateBuilderWithEarliestMode() {
            var props = new FlussSourceProperties(
                "my-source",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                FlussStartupMode.EARLIEST,
                null,
                Map.of("client.scanner.fetch.max-bytes", "1048576")
            );

            var builder = FlussSourceFactory.supplyBuilderFor(props, TEST_SCHEMA);
            assertNotNull(builder);

            assertThrows(RuntimeException.class, () -> FlussSourceFactory.supplyFor(props, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should successfully create FlussSourceBuilder with LATEST mode")
        void shouldCreateBuilderWithLatestMode() {
            var props = new FlussSourceProperties(
                "my-source",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                FlussStartupMode.LATEST,
                null,
                Map.of()
            );

            var builder = FlussSourceFactory.supplyBuilderFor(props, TEST_SCHEMA);
            assertNotNull(builder);

            assertThrows(RuntimeException.class, () -> FlussSourceFactory.supplyFor(props, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should successfully create FlussSourceBuilder with FULL mode")
        void shouldCreateBuilderWithFullMode() {
            var props = new FlussSourceProperties(
                "my-source",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                FlussStartupMode.FULL,
                null,
                Map.of()
            );

            var builder = FlussSourceFactory.supplyBuilderFor(props, TEST_SCHEMA);
            assertNotNull(builder);

            assertThrows(RuntimeException.class, () -> FlussSourceFactory.supplyFor(props, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should successfully create FlussSourceBuilder with TIMESTAMP mode")
        void shouldCreateBuilderWithTimestampMode() {
            var props = new FlussSourceProperties(
                "my-source",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                FlussStartupMode.TIMESTAMP,
                1700000000000L,
                Map.of()
            );

            var builder = FlussSourceFactory.supplyBuilderFor(props, TEST_SCHEMA);
            assertNotNull(builder);

            assertThrows(RuntimeException.class, () -> FlussSourceFactory.supplyFor(props, TEST_SCHEMA));
        }

        @Test
        @DisplayName("Should throw NullPointerException when parameters are null")
        void shouldThrowNpeWhenParametersAreNull() {
            var props = new FlussSourceProperties(
                "my-source",
                List.of("localhost:9123"),
                "my_db",
                "my_table",
                FlussStartupMode.EARLIEST,
                null,
                Map.of()
            );

            assertAll(
                () -> assertThrows(NullPointerException.class, () -> FlussSourceFactory.supplyBuilderFor(null, TEST_SCHEMA)),
                () -> assertThrows(NullPointerException.class, () -> FlussSourceFactory.supplyBuilderFor(props, null)),
                () -> assertThrows(NullPointerException.class, () -> FlussSourceFactory.supplyFor(null, TEST_SCHEMA)),
                () -> assertThrows(NullPointerException.class, () -> FlussSourceFactory.supplyFor(props, null))
            );
        }
    }
}
