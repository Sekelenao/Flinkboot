package io.github.sekelenao.flinkboot.kafka.api.sink;

import io.github.sekelenao.flinkboot.kafka.api.configuration.KafkaSinkConfiguration;
import io.github.sekelenao.flinkboot.kafka.internal.DeliveryGuaranteeMapper;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;

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
        var deliveryGuarantee = DeliveryGuaranteeMapper.map(config);

        var builder = KafkaSink.<T>builder()
            .setBootstrapServers(String.join(",", config.bootstrapServers()))
            .setRecordSerializer(serializationSchema)
            .setDeliveryGuarantee(deliveryGuarantee)
            .setKafkaProducerConfig(additionalProperties);

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
