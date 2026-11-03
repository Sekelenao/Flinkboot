package com.flinkboot.kafka.api.source;

import com.flinkboot.kafka.api.properties.source.KafkaBoundedness;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import com.flinkboot.kafka.internal.OffsetInitializerMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;

import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;
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

    private KafkaSourceFactory() {
        throw new AssertionError("You cannot instantiate this class");
    }

    /**
     * Creates and pre-configures a {@link KafkaSourceBuilder} from the provided configuration properties.
     *
     * @param config the Kafka source configuration properties
     * @param schema the deserialization schema to decode Kafka records
     * @param <T>    the deserialized record type
     * @return a pre-configured {@link KafkaSourceBuilder}
     * @throws NullPointerException if {@code config} or {@code schema} is {@code null}
     */
    public static <T> KafkaSourceBuilder<T> supplyBuilderFor(
        KafkaSourceProperties config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(schema, "schema must not be null");

        var additionalProperties = new Properties();
        additionalProperties.putAll(config.properties());
        var startingOffsets = OffsetInitializerMapper.map(config.startingOffsets());

        var builder = KafkaSource.<T>builder()
            .setBootstrapServers(String.join(",", config.bootstrapServers()))
            .setGroupId(config.groupId())
            .setStartingOffsets(startingOffsets)
            .setProperties(additionalProperties)
            .setDeserializer(schema);

        config.clientIdPrefix().ifPresent(builder::setClientIdPrefix);

        config.stoppingOffsets().ifPresent(stopping -> {
            var stoppingOffsets = OffsetInitializerMapper.map(stopping);
            if (config.boundedness().orElse(KafkaBoundedness.UNBOUNDED) == KafkaBoundedness.BOUNDED) {
                builder.setBounded(stoppingOffsets);
            } else {
                builder.setUnbounded(stoppingOffsets);
            }
        });

        if (!config.topics().isEmpty()) {
            builder.setTopics(config.topics());
            return builder;
        }
        builder.setTopicPattern(Pattern.compile(config.topicPattern().orElseThrow()));
        return builder;
    }

    /**
     * Creates and builds a {@link KafkaSource} from the provided configuration properties.
     *
     * @param config the Kafka source configuration properties
     * @param schema the deserialization schema to decode Kafka records
     * @param <T>    the deserialized record type
     * @return a built and immutable {@link KafkaSource}
     * @throws NullPointerException if {@code config} or {@code schema} is {@code null}
     */
    public static <T> KafkaSource<T> supplyFor(
        KafkaSourceProperties config,
        KafkaRecordDeserializationSchema<T> schema
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(schema, "schema must not be null");
        return supplyBuilderFor(config, schema).build();
    }

    /**
     * Creates and pre-configures a {@link KafkaSourceBuilder} from the provided configuration properties
     * using a value-only deserialization schema.
     *
     * @param config the Kafka source configuration properties
     * @param schema the deserialization schema to decode Kafka record values
     * @param <T>    the deserialized record type
     * @return a pre-configured {@link KafkaSourceBuilder}
     * @throws NullPointerException if {@code config} or {@code schema} is {@code null}
     */
    public static <T> KafkaSourceBuilder<T> supplyBuilderFor(
        KafkaSourceProperties config,
        DeserializationSchema<T> schema
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(schema, "schema must not be null");
        return supplyBuilderFor(config, KafkaRecordDeserializationSchema.valueOnly(schema));
    }

    /**
     * Creates and builds a {@link KafkaSource} from the provided configuration properties using a value-only
     * deserialization schema.
     *
     * @param config the Kafka source configuration properties
     * @param schema the deserialization schema to decode Kafka record values
     * @param <T>    the deserialized record type
     * @return a built and immutable {@link KafkaSource}
     * @throws NullPointerException if {@code config} or {@code schema} is {@code null}
     */
    public static <T> KafkaSource<T> supplyFor(
        KafkaSourceProperties config,
        DeserializationSchema<T> schema
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(schema, "schema must not be null");
        return supplyBuilderFor(config, schema).build();
    }

    /**
     * Creates and builds a {@link KafkaSource} from the provided configuration properties using a value-only
     * deserialization schema after applying a customizer to its builder.
     *
     * @param config the Kafka source configuration properties
     * @param schema the deserialization schema to decode Kafka record values
     * @param builderCustomizer the customizer applied to the pre-configured source builder before construction
     * @param <T>    the deserialized record type
     * @return a built and immutable {@link KafkaSource}
     * @throws NullPointerException if {@code config}, {@code schema}, or {@code builderCustomizer} is {@code null}
     */
    public static <T> KafkaSource<T> supplyFor(
        KafkaSourceProperties config,
        DeserializationSchema<T> schema,
        Consumer<KafkaSourceBuilder<T>> builderCustomizer
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(schema, "schema must not be null");
        Objects.requireNonNull(builderCustomizer, "builderCustomizer must not be null");

        var builder = supplyBuilderFor(config, schema);
        builderCustomizer.accept(builder);
        return builder.build();
    }
}
