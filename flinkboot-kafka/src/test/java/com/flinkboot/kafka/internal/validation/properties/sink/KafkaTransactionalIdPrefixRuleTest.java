package com.flinkboot.kafka.internal.validation.properties.sink;

import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.kafka.api.properties.sink.KafkaDeliveryGuarantee;
import com.flinkboot.kafka.api.properties.sink.KafkaSinkProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("KafkaTransactionalIdPrefixRule")
class KafkaTransactionalIdPrefixRuleTest {

    private final KafkaTransactionalIdPrefixRule rule = new KafkaTransactionalIdPrefixRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when delivery-guarantee is EXACTLY_ONCE without transactional-id-prefix")
    void shouldFailWhenExactlyOnceWithoutPrefix() {
        var props = new KafkaSinkProperties(
            null, List.of("localhost:9092"), "topic", KafkaDeliveryGuarantee.EXACTLY_ONCE, null, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "transactional-id-prefix",
                "transactional-id-prefix is required when delivery-guarantee is EXACTLY_ONCE"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should fail when delivery-guarantee is not EXACTLY_ONCE with transactional-id-prefix")
    void shouldFailWhenAtLeastOnceWithPrefix() {
        var props = new KafkaSinkProperties(
            null, List.of("localhost:9092"), "topic", KafkaDeliveryGuarantee.AT_LEAST_ONCE, "tx-prefix", null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "transactional-id-prefix",
                "transactional-id-prefix can only be specified when delivery-guarantee is EXACTLY_ONCE"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when delivery-guarantee is EXACTLY_ONCE with transactional-id-prefix")
    void shouldPassWhenExactlyOnceWithPrefix() {
        var props = new KafkaSinkProperties(
            null, List.of("localhost:9092"), "topic", KafkaDeliveryGuarantee.EXACTLY_ONCE, "tx-prefix", null
        );

        assertTrue(rule.check(props).isEmpty());
    }
}
