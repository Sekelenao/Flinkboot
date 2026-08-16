package io.github.sekelenao.flinkboot.kafka.api.sink;

import io.github.sekelenao.flinkboot.kafka.api.properties.sink.KafkaSinkProperties;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder;

import java.util.Objects;
import java.util.Properties;

/**
 * Factory utility for constructing Apache Flink {@link KafkaSink} instances from configuration properties.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * KafkaSink<String> sink = KafkaSinkFactory.supplyFor(
 *     config.kafkaSink(),
 *     KafkaRecordSerializationSchema.builder()
 *         .setTopic(config.kafkaSink().topic())
 *         .setValueSerializationSchema(new SimpleStringSchema())
 *         .build()
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
        Objects.requireNonNull(config);
        Objects.requireNonNull(serializationSchema);

        var additionalProperties = new Properties();
        additionalProperties.putAll(config.properties());

        var builder = KafkaSink.<T>builder()
            .setBootstrapServers(String.join(",", config.bootstrapServers()))
            .setRecordSerializer(serializationSchema)
            .setKafkaProducerConfig(additionalProperties);

        config.deliveryGuarantee().ifPresent(guarantee -> builder.setDeliveryGuarantee(guarantee.deliveryGuarantee()));
        config.transactionalIdPrefix().ifPresent(builder::setTransactionalIdPrefix);

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
        return supplyBuilderFor(config, serializationSchema).build();
    }
}
