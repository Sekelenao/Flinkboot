package com.flinkboot.kafka.internal.validation.properties.sink;

import com.flinkboot.kafka.api.properties.sink.KafkaDeliveryGuarantee;
import com.flinkboot.kafka.api.properties.sink.KafkaSinkProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("KafkaSinkPropertiesValidator")
class KafkaSinkPropertiesValidatorTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Should throw AssertionError when trying to instantiate via reflection")
        void shouldThrowWhenInstantiatedViaReflection() throws Exception {
            var constructor = KafkaSinkPropertiesValidator.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            var targetException = assertThrows(InvocationTargetException.class, constructor::newInstance);
            assertInstanceOf(AssertionError.class, targetException.getCause());
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should return true when all Kafka sink rules pass")
        void shouldPassWhenAllRulesPass() {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSinkProperties(
                "sink",
                List.of("localhost:9092"),
                "topic",
                KafkaDeliveryGuarantee.EXACTLY_ONCE,
                "my-prefix",
                Map.of()
            );

            assertTrue(KafkaSinkPropertiesValidator.validate(props, context));
        }

        @Test
        @DisplayName("Should fail when Kafka sink rule fails")
        void shouldFailWhenTransactionalIdPrefixRuleFails() {
            var context = mock(ConstraintValidatorContext.class, Answers.RETURNS_DEEP_STUBS);
            var props = new KafkaSinkProperties(
                "sink",
                List.of("localhost:9092"),
                "topic",
                KafkaDeliveryGuarantee.EXACTLY_ONCE,
                null,
                Map.of()
            );

            assertFalse(KafkaSinkPropertiesValidator.validate(props, context));
            verify(context.buildConstraintViolationWithTemplate(
                "transactional-id-prefix is required when delivery-guarantee is EXACTLY_ONCE"
            )).addPropertyNode("transactional-id-prefix");
        }
    }
}
