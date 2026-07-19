package io.github.sekelenao.flinkboot.kafka.api.configuration.sink;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
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

public class KafkaSinkConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty
    private final List<String> bootstrapServers;

    @NotBlank
    private final String topic;

    private final KafkaDeliveryGuarantee deliveryGuarantee;

    @Pattern(regexp = "\\s*\\S.*", message = "must not be blank")
    private final String transactionalIdPrefix;

    private final Map<@NotNull String, @NotNull String> properties;

    @JsonCreator
    public KafkaSinkConfiguration(
        @JsonProperty("bootstrap-servers") List<String> bootstrapServers,
        @JsonProperty("topic") String topic,
        @JsonProperty("delivery-guarantee") KafkaDeliveryGuarantee deliveryGuarantee,
        @JsonProperty("transactional-id-prefix") String transactionalIdPrefix,
        @JsonProperty("properties") Map<String, String> properties
    ) {
        this.bootstrapServers = Objects.requireNonNull(bootstrapServers);
        this.topic = Objects.requireNonNull(topic);
        this.deliveryGuarantee = deliveryGuarantee;
        this.transactionalIdPrefix = transactionalIdPrefix;
        this.properties = properties;
    }

    public List<String> bootstrapServers() {
        if (bootstrapServers == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(bootstrapServers);
    }

    public String topic() {
        return topic;
    }

    public Optional<KafkaDeliveryGuarantee> deliveryGuarantee() {
        return Optional.ofNullable(deliveryGuarantee);
    }

    public Optional<String> transactionalIdPrefix() {
        if (transactionalIdPrefix == null || transactionalIdPrefix.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(transactionalIdPrefix);
    }

    public Map<String, String> properties() {
        if (properties == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(properties);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof KafkaSinkConfiguration)) {
            return false;
        }
        var o = (KafkaSinkConfiguration) other;
        return Objects.equals(bootstrapServers, o.bootstrapServers)
            && Objects.equals(topic, o.topic)
            && deliveryGuarantee == o.deliveryGuarantee
            && Objects.equals(transactionalIdPrefix, o.transactionalIdPrefix)
            && Objects.equals(properties, o.properties);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(bootstrapServers, topic, deliveryGuarantee, transactionalIdPrefix, properties);
    }

    @Override
    @Generated
    public String toString() {
        return "KafkaSinkConfiguration{" +
            "bootstrapServers=" + bootstrapServers +
            ", topic='" + topic + '\'' +
            ", deliveryGuarantee=" + deliveryGuarantee +
            ", transactionalIdPrefix='" + transactionalIdPrefix + '\'' +
            ", properties=" + properties +
            '}';
    }
}
