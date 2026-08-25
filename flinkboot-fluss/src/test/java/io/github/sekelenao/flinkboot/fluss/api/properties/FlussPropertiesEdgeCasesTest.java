package io.github.sekelenao.flinkboot.fluss.api.properties;

import io.github.sekelenao.flinkboot.fluss.api.properties.sink.FlussSinkProperties;
import io.github.sekelenao.flinkboot.fluss.api.properties.source.FlussSourceProperties;
import io.github.sekelenao.flinkboot.fluss.api.properties.source.FlussStartupMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Fluss Configuration Edge Cases Tests")
class FlussPropertiesEdgeCasesTest {

    @Test
    @DisplayName("Should test equals, hashCode, and toString on all Fluss DTOs")
    void shouldTestEqualsHashCodeToStringOnFlussDtos() {
        var src1 = new FlussSourceProperties("src", List.of("localhost:9123"), "db", "tbl", FlussStartupMode.EARLIEST, null, Map.of("k", "v"));
        var src2 = new FlussSourceProperties("src", List.of("localhost:9123"), "db", "tbl", FlussStartupMode.EARLIEST, null, Map.of("k", "v"));
        var src3 = new FlussSourceProperties("src-other", List.of("localhost:9123"), "db2", "tbl2", FlussStartupMode.LATEST, null, null);

        assertAll(
            () -> assertEquals(src1, src1),
            () -> assertEquals(src1, src2),
            () -> assertNotEquals(src1, src3),
            () -> assertNotEquals(src1, null),
            () -> assertNotEquals(src1, "other"),
            () -> assertEquals(src1.hashCode(), src2.hashCode()),
            () -> assertNotNull(src1.toString())
        );

        var sink1 = new FlussSinkProperties("snk", List.of("localhost:9123"), "db", "tbl", 1024L, Duration.ofMillis(50), Map.of("k", "v"));
        var sink2 = new FlussSinkProperties("snk", List.of("localhost:9123"), "db", "tbl", 1024L, Duration.ofMillis(50), Map.of("k", "v"));
        var sink3 = new FlussSinkProperties("snk-other", List.of("localhost:9123"), "db2", "tbl2", null, null, null);

        assertAll(
            () -> assertEquals(sink1, sink1),
            () -> assertEquals(sink1, sink2),
            () -> assertNotEquals(sink1, sink3),
            () -> assertNotEquals(null, sink1),
            () -> assertNotEquals("other", sink1),
            () -> assertEquals(sink1.hashCode(), sink2.hashCode()),
            () -> assertNotNull(sink1.toString())
        );
    }

    @Test
    @DisplayName("Should test getters with null or empty optional fields")
    void shouldTestGettersWithNullOrEmptyFields() {
        var nullSrc = new FlussSourceProperties("src", List.of("localhost:9123"), "db", "tbl", FlussStartupMode.EARLIEST, null, null);
        var nullSink = new FlussSinkProperties("snk", List.of("localhost:9123"), "db", "tbl", null, null, null);

        assertAll(
            () -> assertTrue(nullSrc.properties().isEmpty()),
            () -> assertTrue(nullSrc.startupTimestamp().isEmpty()),
            () -> assertTrue(nullSink.properties().isEmpty()),
            () -> assertTrue(nullSink.batchSize().isEmpty()),
            () -> assertTrue(nullSink.batchTimeout().isEmpty())
        );
    }
}

