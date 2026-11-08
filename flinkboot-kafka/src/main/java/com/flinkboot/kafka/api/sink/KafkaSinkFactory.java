package com.flinkboot.kafka.api.sink;

import com.flinkboot.kafka.api.properties.sink.KafkaSinkProperties;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchemaBuilder;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder;

import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Factory utility for constructing Apache Flink {@link KafkaSink} instances from configuration properties.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * KafkaSink<String> sink = KafkaSinkFactory.supplyFor(
 *     config.kafkaSink(),
 *     new SimpleStringSchema()
 * );
 *
 * stream.sinkTo(sink).name(config.kafkaSink().name());
 * }</pre>
 */
public final class KafkaSinkFactory {

    private KafkaSinkFactory() {
        throw new AssertionError("You cannot instantiate this class");
    }

    /**
     * Creates and pre-configures a {@link KafkaSinkBuilder} from configuration properties.
     *
     * @param config              the sink configuration properties
     * @param serializationSchema the serialization schema to encode records for Kafka
     * @param <T>                 the input record type
     * @return a pre-configured {@link KafkaSinkBuilder}
     * @throws NullPointerException if {@code config} or {@code serializationSchema} is {@code null}
     */
    public static <T> KafkaSinkBuilder<T> supplyBuilderFor(
        KafkaSinkProperties config,
        KafkaRecordSerializationSchema<T> serializationSchema
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(serializationSchema, "serializationSchema must not be null");

        var builder = KafkaSink.<T>builder()
            .setBootstrapServers(String.join(",", config.bootstrapServers()))
            .setRecordSerializer(serializationSchema)
            .setDeliveryGuarantee(config.deliveryGuarantee().deliveryGuarantee());

        config.transactionalIdPrefix().ifPresent(builder::setTransactionalIdPrefix);

        var additionalProperties = new Properties();
        additionalProperties.putAll(config.properties());
        builder.setKafkaProducerConfig(additionalProperties);

        return builder;
    }

    /**
     * Creates and builds a {@link KafkaSink} from configuration properties.
     *
     * @param config              the sink configuration properties
     * @param serializationSchema the serialization schema to encode records for Kafka
     * @param <T>                 the input record type
     * @return a built and immutable {@link KafkaSink}
     * @throws NullPointerException if {@code config} or {@code serializationSchema} is {@code null}
     */
    public static <T> KafkaSink<T> supplyFor(
        KafkaSinkProperties config,
        KafkaRecordSerializationSchema<T> serializationSchema
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(serializationSchema, "serializationSchema must not be null");
        return supplyBuilderFor(config, serializationSchema).build();
    }

    /**
     * Creates and pre-configures a {@link KafkaSinkBuilder} from configuration properties
     * using a customizer for the record serialization schema builder pre-configured with the topic.
     *
     * @param config           the sink configuration properties
     * @param schemaCustomizer the customizer applied to the pre-configured schema builder
     * @param <T>              the input record type
     * @return a pre-configured {@link KafkaSinkBuilder}
     * @throws NullPointerException if {@code config} or {@code schemaCustomizer} is {@code null}
     */
    public static <T> KafkaSinkBuilder<T> supplyBuilderFor(
        KafkaSinkProperties config,
        Consumer<KafkaRecordSerializationSchemaBuilder<T>> schemaCustomizer
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(schemaCustomizer, "schemaCustomizer must not be null");

        var schemaBuilder = KafkaRecordSerializationSchema.<T>builder()
            .setTopic(config.topic());
        schemaCustomizer.accept(schemaBuilder);

        return supplyBuilderFor(config, schemaBuilder.build());
    }

    /**
     * Creates and builds a {@link KafkaSink} from configuration properties
     * using a customizer for the record serialization schema builder pre-configured with the topic.
     *
     * @param config           the sink configuration properties
     * @param schemaCustomizer the customizer applied to the pre-configured schema builder
     * @param <T>              the input record type
     * @return a built and immutable {@link KafkaSink}
     * @throws NullPointerException if {@code config} or {@code schemaCustomizer} is {@code null}
     */
    public static <T> KafkaSink<T> supplyFor(
        KafkaSinkProperties config,
        Consumer<KafkaRecordSerializationSchemaBuilder<T>> schemaCustomizer
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(schemaCustomizer, "schemaCustomizer must not be null");
        return supplyBuilderFor(config, schemaCustomizer).build();
    }

    /**
     * Creates and pre-configures a {@link KafkaSinkBuilder} from configuration properties
     * using a value-only serialization schema targeting the configured topic.
     *
     * @param config                   the sink configuration properties
     * @param valueSerializationSchema the serialization schema to encode record values
     * @param <T>                      the input record type
     * @return a pre-configured {@link KafkaSinkBuilder}
     * @throws NullPointerException if {@code config} or {@code valueSerializationSchema} is {@code null}
     */
    public static <T> KafkaSinkBuilder<T> supplyBuilderFor(
        KafkaSinkProperties config,
        SerializationSchema<T> valueSerializationSchema
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(valueSerializationSchema, "valueSerializationSchema must not be null");
        Consumer<KafkaRecordSerializationSchemaBuilder<T>> customizer =
            builder -> builder.setValueSerializationSchema(valueSerializationSchema);
        return supplyBuilderFor(config, customizer);
    }

    /**
     * Creates and builds a {@link KafkaSink} from configuration properties
     * using a value-only serialization schema targeting the configured topic.
     *
     * @param config                   the sink configuration properties
     * @param valueSerializationSchema the serialization schema to encode record values
     * @param <T>                      the input record type
     * @return a built and immutable {@link KafkaSink}
     * @throws NullPointerException if {@code config} or {@code valueSerializationSchema} is {@code null}
     */
    public static <T> KafkaSink<T> supplyFor(
        KafkaSinkProperties config,
        SerializationSchema<T> valueSerializationSchema
    ) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(valueSerializationSchema, "valueSerializationSchema must not be null");
        return supplyBuilderFor(config, valueSerializationSchema).build();
    }
}
