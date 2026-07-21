package io.github.sekelenao.flinkboot.kafka.api.sink;

import io.github.sekelenao.flinkboot.kafka.api.configuration.sink.KafkaSinkConfiguration;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder;

import java.util.Objects;
import java.util.Properties;

public final class KafkaSinkFactory {

    private KafkaSinkFactory() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static <T> KafkaSinkBuilder<T> supplyBuilderFor(
        KafkaSinkConfiguration config,
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

    public static <T> KafkaSink<T> supplyFor(
        KafkaSinkConfiguration config,
        KafkaRecordSerializationSchema<T> serializationSchema
    ) {
        return supplyBuilderFor(config, serializationSchema).build();
    }
}
