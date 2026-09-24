package io.github.sekelenao.flinkboot.kafka.api.properties.sink;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.api.validation.ValidatableProperties;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import io.github.sekelenao.flinkboot.kafka.internal.validation.properties.KafkaSinkPropertiesValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration properties for Kafka producer sinks in Apache Flink.
 */
public final class KafkaSinkProperties implements Serializable, ValidatableProperties {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String name;

    @NotEmpty
    private final List<@NotBlank String> bootstrapServers;

    @NotBlank
    private final String topic;

    @NotNull
    private final KafkaDeliveryGuarantee deliveryGuarantee;

    @Pattern(regexp = "\\s*\\S.*", message = "must not be blank")
    private final String transactionalIdPrefix;

    private final Map<@NotNull String, @NotNull String> properties;

    /**
     * Creates a new {@code KafkaSinkProperties} instance.
     *
     * @param name                  sink operator name in Flink DAG
     * @param bootstrapServers      list of Kafka broker addresses
     * @param topic                 target Kafka topic name
     * @param deliveryGuarantee     delivery guarantee (NONE, AT_LEAST_ONCE, EXACTLY_ONCE)
     * @param transactionalIdPrefix prefix for Kafka transactions (required if deliveryGuarantee is EXACTLY_ONCE)
     * @param properties            additional Kafka producer client properties
     */
    @JsonCreator
    public KafkaSinkProperties(
        @JsonProperty("name") String name,
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("topic") String topic,
        @JsonProperty("delivery-guarantee") KafkaDeliveryGuarantee deliveryGuarantee,
        @JsonProperty("transactional-id-prefix") String transactionalIdPrefix,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.name = name;
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.deliveryGuarantee = deliveryGuarantee;
        this.transactionalIdPrefix = transactionalIdPrefix;
        this.properties = properties;
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        return KafkaSinkPropertiesValidator.validate(this, context);
    }

    /**
     * Returns the sink operator name.
     *
     * @return the name string
     */
    public String name() {
        return name;
    }

    /**
     * Returns the list of Kafka bootstrap servers.
     *
     * @return an unmodifiable list of broker addresses
     */
    public List<String> bootstrapServers() {
        if (bootstrapServers == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(bootstrapServers);
    }

    /**
     * Returns the target Kafka topic name.
     *
     * @return the topic name
     */
    public String topic() {
        return topic;
    }

    /**
     * Returns the delivery guarantee semantic.
     *
     * @return the {@link KafkaDeliveryGuarantee}
     */
    public KafkaDeliveryGuarantee deliveryGuarantee() {
        return deliveryGuarantee;
    }

    /**
     * Returns the transactional ID prefix for exactly-once producer transactions.
     *
     * @return an {@link Optional} containing the prefix string, or empty if not specified
     */
    public Optional<String> transactionalIdPrefix() {
        return Optional.ofNullable(transactionalIdPrefix);
    }

    /**
     * Returns additional Kafka producer client properties.
     *
     * @return an unmodifiable map of configuration properties
     */
    public Map<String, String> properties() {
        if (properties == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(properties);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof KafkaSinkProperties)) {
            return false;
        }
        var o = (KafkaSinkProperties) other;
        return Objects.equals(name, o.name)
            && Objects.equals(bootstrapServers, o.bootstrapServers)
            && Objects.equals(topic, o.topic)
            && deliveryGuarantee == o.deliveryGuarantee
            && Objects.equals(transactionalIdPrefix, o.transactionalIdPrefix)
            && Objects.equals(properties, o.properties);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(name, bootstrapServers, topic, deliveryGuarantee, transactionalIdPrefix, properties);
    }

    @Override
    @Generated
    public String toString() {
        return "KafkaSinkProperties{" +
            "name='" + name + '\'' +
            ", bootstrapServers=" + bootstrapServers +
            ", topic='" + topic + '\'' +
            ", deliveryGuarantee=" + deliveryGuarantee +
            ", transactionalIdPrefix='" + transactionalIdPrefix + '\'' +
            ", properties=" + properties +
            '}';
    }
}
