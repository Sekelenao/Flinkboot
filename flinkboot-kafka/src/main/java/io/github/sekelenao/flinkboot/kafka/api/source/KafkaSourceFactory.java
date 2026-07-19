package io.github.sekelenao.flinkboot.kafka.api.source;

import io.github.sekelenao.flinkboot.kafka.api.configuration.source.KafkaSourceTopicListConfiguration;
import io.github.sekelenao.flinkboot.kafka.api.configuration.source.KafkaSourceTopicPatternConfiguration;
import io.github.sekelenao.flinkboot.kafka.internal.OffsetInitializerMapper;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;

import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

public final class KafkaSourceFactory {

    private KafkaSourceFactory(){
        throw new AssertionError("You cannot instantiate this class");
    }

    public static <T> KafkaSourceBuilder<T> supplyBuilderFor(
        KafkaSourceTopicListConfiguration config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(schema);

        var additionalProperties = new Properties();
        additionalProperties.putAll(config.properties());
        var startingOffsets = OffsetInitializerMapper.map(config);

        return KafkaSource.<T>builder()
            .setBootstrapServers(String.join(",", config.bootstrapServers()))
            .setGroupId(config.groupId())
            .setTopics(config.topics())
            .setStartingOffsets(startingOffsets)
            .setProperties(additionalProperties)
            .setDeserializer(schema);
    }

    public static <T> KafkaSource<T> supplyFor(
        KafkaSourceTopicListConfiguration config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        return supplyBuilderFor(config, schema).build();
    }

    public static <T> KafkaSourceBuilder<T> supplyBuilderFor(
        KafkaSourceTopicPatternConfiguration config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(schema);

        var additionalProperties = new Properties();
        additionalProperties.putAll(config.properties());
        var startingOffsets = OffsetInitializerMapper.map(config);

        return KafkaSource.<T>builder()
            .setBootstrapServers(String.join(",", config.bootstrapServers()))
            .setGroupId(config.groupId())
            .setTopicPattern(Pattern.compile(config.topicPattern()))
            .setStartingOffsets(startingOffsets)
            .setProperties(additionalProperties)
            .setDeserializer(schema);
    }

    public static <T> KafkaSource<T> supplyFor(
        KafkaSourceTopicPatternConfiguration config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        return supplyBuilderFor(config, schema).build();
    }

}
