package io.github.sekelenao.flinkboot.kafka.internal;

import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.TopicPartitionOffsetProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("OffsetInitializerMapper")
class OffsetInitializerMapperTest {

    @Test
    @DisplayName("Private constructor should throw AssertionError")
    void testConstructorIsPrivate() throws Exception {
        var constructor = OffsetInitializerMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(AssertionError.class, exception.getCause());
    }

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var ex = assertThrows(NullPointerException.class, () -> OffsetInitializerMapper.map(null));
        assertEquals("properties must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("Should successfully map all offset initializer strategies")
    void shouldMapAllOffsetInitializerStrategies() {
        var earliest = new KafkaOffsetProperties(KafkaOffsetInitializer.EARLIEST, null, null);
        var latest = new KafkaOffsetProperties(KafkaOffsetInitializer.LATEST, null, null);
        var committed = new KafkaOffsetProperties(KafkaOffsetInitializer.COMMITTED, null, null);
        var committedEarliest = new KafkaOffsetProperties(KafkaOffsetInitializer.COMMITTED_EARLIEST, null, null);
        var committedLatest = new KafkaOffsetProperties(KafkaOffsetInitializer.COMMITTED_LATEST, null, null);
        var timestamp = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 1689717600000L, null);
        var partition = new TopicPartitionOffsetProperties("test-topic", 0, 100L);
        var offsets = new KafkaOffsetProperties(KafkaOffsetInitializer.OFFSETS, null, List.of(partition));

        assertAll(
            () -> assertEquals(KafkaOffsetInitializer.EARLIEST.offsetsInitializer().orElseThrow(), OffsetInitializerMapper.map(earliest)),
            () -> assertEquals(KafkaOffsetInitializer.LATEST.offsetsInitializer().orElseThrow(), OffsetInitializerMapper.map(latest)),
            () -> assertEquals(KafkaOffsetInitializer.COMMITTED.offsetsInitializer().orElseThrow(), OffsetInitializerMapper.map(committed)),
            () -> assertEquals(KafkaOffsetInitializer.COMMITTED_EARLIEST.offsetsInitializer().orElseThrow(), OffsetInitializerMapper.map(committedEarliest)),
            () -> assertEquals(KafkaOffsetInitializer.COMMITTED_LATEST.offsetsInitializer().orElseThrow(), OffsetInitializerMapper.map(committedLatest)),
            () -> assertNotNull(OffsetInitializerMapper.map(timestamp)),
            () -> assertNotNull(OffsetInitializerMapper.map(offsets))
        );
    }
}
