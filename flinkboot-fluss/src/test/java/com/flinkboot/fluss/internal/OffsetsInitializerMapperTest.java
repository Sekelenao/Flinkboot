package com.flinkboot.fluss.internal;

import com.flinkboot.fluss.api.properties.source.FlussSourceProperties;
import com.flinkboot.fluss.api.properties.source.FlussStartupMode;
import org.apache.fluss.client.initializer.TimestampOffsetsInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("OffsetsInitializerMapper")
class OffsetsInitializerMapperTest {

    @Test
    @DisplayName("Private constructor should throw AssertionError")
    void testConstructorIsPrivate() throws Exception {
        var constructor = OffsetsInitializerMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
    }

    @Test
    @DisplayName("Should throw NullPointerException when config is null")
    void shouldThrowWhenConfigIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> OffsetsInitializerMapper.map(null));
        assertEquals("config must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = FlussStartupMode.class, names = {"EARLIEST", "LATEST", "FULL"})
    @DisplayName("Should map static startup modes to their OffsetsInitializer")
    void shouldMapStaticStartupModes(FlussStartupMode mode) {
        var config = new FlussSourceProperties(
            "my-source",
            List.of("localhost:9123"),
            "my_db",
            "my_table",
            mode,
            null,
            Map.of()
        );
        assertEquals(mode.offsetsInitializer().orElseThrow(), OffsetsInitializerMapper.map(config));
    }

    @Test
    @DisplayName("Should map TIMESTAMP startup mode when startupTimestamp is present")
    void shouldMapTimestampStartupMode() {
        var config = new FlussSourceProperties(
            "my-source",
            List.of("localhost:9123"),
            "my_db",
            "my_table",
            FlussStartupMode.TIMESTAMP,
            1700000000000L,
            Map.of()
        );
        assertInstanceOf(TimestampOffsetsInitializer.class, OffsetsInitializerMapper.map(config));
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when TIMESTAMP mode has no startupTimestamp")
    void shouldThrowWhenTimestampModeHasNoTimestamp() {
        var config = new FlussSourceProperties(
            "my-source",
            List.of("localhost:9123"),
            "my_db",
            "my_table",
            FlussStartupMode.TIMESTAMP,
            null,
            Map.of()
        );
        assertThrows(NoSuchElementException.class, () -> OffsetsInitializerMapper.map(config));
    }
}
