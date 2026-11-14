package com.flinkboot.kafka.internal;

import com.flinkboot.core.api.exception.configuration.ConfigurationValidationException;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetInitializer;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetProperties;
import com.flinkboot.kafka.api.properties.source.KafkaOffsetResetStrategy;
import com.flinkboot.kafka.api.properties.source.TopicPartitionOffsetProperties;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        var earliest = new KafkaOffsetProperties(KafkaOffsetInitializer.EARLIEST, null, null, null);
        var latest = new KafkaOffsetProperties(KafkaOffsetInitializer.LATEST, null, null, null);
        var committed = new KafkaOffsetProperties(KafkaOffsetInitializer.COMMITTED, null, null, null);
        var committedEarliest = new KafkaOffsetProperties(KafkaOffsetInitializer.COMMITTED_EARLIEST, null, null, null);
        var committedLatest = new KafkaOffsetProperties(KafkaOffsetInitializer.COMMITTED_LATEST, null, null, null);
        var timestamp = new KafkaOffsetProperties(KafkaOffsetInitializer.TIMESTAMP, 1689717600000L, null, null);
        var partition = new TopicPartitionOffsetProperties("test-topic", 0, 100L);
        var offsets = new KafkaOffsetProperties(
            KafkaOffsetInitializer.OFFSETS, null, List.of(partition), KafkaOffsetResetStrategy.EARLIEST
        );

        assertAll(
            () -> assertEquals(KafkaOffsetInitializer.EARLIEST.offsetsInitializer().orElseThrow(), OffsetInitializerMapper.map(earliest)),
            () -> assertEquals(KafkaOffsetInitializer.LATEST.offsetsInitializer().orElseThrow(), OffsetInitializerMapper.map(latest)),
            () -> assertEquals(KafkaOffsetInitializer.COMMITTED.offsetsInitializer().orElseThrow(), OffsetInitializerMapper.map(committed)),
            () -> assertEquals(KafkaOffsetInitializer.COMMITTED_EARLIEST.offsetsInitializer().orElseThrow(), OffsetInitializerMapper.map(committedEarliest)),
            () -> assertEquals(KafkaOffsetInitializer.COMMITTED_LATEST.offsetsInitializer().orElseThrow(), OffsetInitializerMapper.map(committedLatest)),
            () -> assertNotNull(OffsetInitializerMapper.map(timestamp)),
            () -> assertNotNull(OffsetInitializerMapper.map(offsets)),
            () -> assertEquals(OffsetResetStrategy.EARLIEST, OffsetInitializerMapper.map(offsets).getAutoOffsetResetStrategy())
        );
    }

    @ParameterizedTest
    @EnumSource(KafkaOffsetResetStrategy.class)
    @DisplayName("Should map configured reset strategy and partition offsets for OFFSETS strategy")
    void shouldMapConfiguredResetStrategyForOffsets(KafkaOffsetResetStrategy strategy) {
        var partition = new TopicPartitionOffsetProperties("test-topic", 0, 100L);
        var props = new KafkaOffsetProperties(
            KafkaOffsetInitializer.OFFSETS, null, List.of(partition), strategy
        );

        var initializer = OffsetInitializerMapper.map(props);
        var topicPartition = new TopicPartition("test-topic", 0);
        var partitionOffsets = initializer.getPartitionOffsets(List.of(topicPartition), null);

        assertAll(
            () -> assertNotNull(initializer),
            () -> assertEquals(strategy.offsetResetStrategy(), initializer.getAutoOffsetResetStrategy()),
            () -> assertNotNull(partitionOffsets),
            () -> assertEquals(100L, partitionOffsets.get(topicPartition))
        );
    }

    @Test
    @DisplayName("Should throw ConfigurationValidationException when duplicate topic-partition offsets are configured")
    void shouldThrowOnDuplicateTopicPartitionOffsets() {
        var first = new TopicPartitionOffsetProperties("test-topic", 0, 100L);
        var duplicate = new TopicPartitionOffsetProperties("test-topic", 0, 200L);
        var props = new KafkaOffsetProperties(
            KafkaOffsetInitializer.OFFSETS, null, List.of(first, duplicate), KafkaOffsetResetStrategy.EARLIEST
        );

        var ex = assertThrows(ConfigurationValidationException.class, () -> OffsetInitializerMapper.map(props));
        assertTrue(ex.getMessage().contains("test-topic-0"));
    }
}
