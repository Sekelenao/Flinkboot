package io.github.sekelenao.flinkboot.kafka.api.source;

import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaSourceTopicListProperties;
import io.github.sekelenao.flinkboot.kafka.api.properties.source.KafkaSourceTopicPatternProperties;
import io.github.sekelenao.flinkboot.kafka.internal.OffsetInitializerMapper;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;

import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Factory utility for constructing Apache Flink {@link KafkaSource} instances from configuration properties.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * KafkaSource<String> source = KafkaSourceFactory.supplyFor(
 *     config.kafkaSource(),
 *     new SimpleStringSchema()
 * );
 *
 * DataStream<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), config.kafkaSource().name());
 * }</pre>
 */
public final class KafkaSourceFactory {

    private KafkaSourceFactory(){
        throw new AssertionError("You cannot instantiate this class");
    }

    /**
     * Creates and pre-configures a {@link KafkaSourceBuilder} for an explicit list of topics.
     *
     * @param config the topic list source configuration properties
     * @param schema the deserialization schema to decode Kafka records
     * @param <T>    the deserialized record type
     * @return a pre-configured {@link KafkaSourceBuilder}
     * @throws NullPointerException if {@code config} or {@code schema} is {@code null}
     */
    public static <T> KafkaSourceBuilder<T> supplyBuilderFor(
        KafkaSourceTopicListProperties config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(schema, "schema must not be null");

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

    /**
     * Creates and builds a {@link KafkaSource} for an explicit list of topics.
     *
     * @param config the topic list source configuration properties
     * @param schema the deserialization schema to decode Kafka records
     * @param <T>    the deserialized record type
     * @return a built and immutable {@link KafkaSource}
     * @throws NullPointerException if {@code config} or {@code schema} is {@code null}
     */
    public static <T> KafkaSource<T> supplyFor(
        KafkaSourceTopicListProperties config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        return supplyBuilderFor(config, schema).build();
    }

    /**
     * Creates and pre-configures a {@link KafkaSourceBuilder} for a dynamic regex topic pattern.
     *
     * @param config the topic pattern source configuration properties
     * @param schema the deserialization schema to decode Kafka records
     * @param <T>    the deserialized record type
     * @return a pre-configured {@link KafkaSourceBuilder}
     * @throws NullPointerException if {@code config} or {@code schema} is {@code null}
     */
    public static <T> KafkaSourceBuilder<T> supplyBuilderFor(
        KafkaSourceTopicPatternProperties config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(schema, "schema must not be null");

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

    /**
     * Creates and builds a {@link KafkaSource} for a dynamic regex topic pattern.
     *
     * @param config the topic pattern source configuration properties
     * @param schema the deserialization schema to decode Kafka records
     * @param <T>    the deserialized record type
     * @return a built and immutable {@link KafkaSource}
     * @throws NullPointerException if {@code config} or {@code schema} is {@code null}
     */
    public static <T> KafkaSource<T> supplyFor(
        KafkaSourceTopicPatternProperties config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        return supplyBuilderFor(config, schema).build();
    }

}
