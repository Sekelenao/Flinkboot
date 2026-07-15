package io.github.sekelenao.flinkboot.kafka.api.source;

import io.github.sekelenao.flinkboot.kafka.api.configuration.KafkaSourceTopicListConfiguration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;

import java.util.Objects;
import java.util.Properties;

public final class KafkaSourceFactory {

    private KafkaSourceFactory(){
        throw new AssertionError("You cannot instantiate this class");
    }

    public static <T> KafkaSource<T> supplyFor(
        KafkaSourceTopicListConfiguration config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(schema);

        var properties = new Properties();
        config.properties().ifPresent(properties::putAll);

        return KafkaSource.<T>builder()
            .setBootstrapServers(String.join(",", config.bootstrapServers()))
            .setGroupId(config.groupId())
            .setTopics(config.topics())
            .setStartingOffsets(config.startingOffsets().offsetsInitializer())
            .setProperties(properties)
            .setDeserializer(schema)
            .build();
    }

}
