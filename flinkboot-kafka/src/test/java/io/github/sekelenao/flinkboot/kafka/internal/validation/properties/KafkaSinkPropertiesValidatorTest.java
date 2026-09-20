package io.github.sekelenao.flinkboot.kafka.internal.validation.properties;

import io.github.sekelenao.flinkboot.kafka.api.properties.sink.KafkaDeliveryGuarantee;
import io.github.sekelenao.flinkboot.kafka.api.properties.sink.KafkaSinkProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        @DisplayName("Should pass when EXACTLY_ONCE is used with non-blank transactionalIdPrefix")
        void shouldPassWhenExactlyOnceWithPrefix() {
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

        @ParameterizedTest
        @NullSource
        @EnumSource(value = KafkaDeliveryGuarantee.class, names = "EXACTLY_ONCE", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should pass when non-EXACTLY_ONCE guarantee has no transactionalIdPrefix")
        void shouldPassWhenNonExactlyOnceWithoutPrefix(KafkaDeliveryGuarantee guarantee) {
            var context = mock(ConstraintValidatorContext.class);
            var props = new KafkaSinkProperties(
                "sink",
                List.of("localhost:9092"),
                "topic",
                guarantee,
                null,
                Map.of()
            );

            assertTrue(KafkaSinkPropertiesValidator.validate(props, context));
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Should fail when EXACTLY_ONCE is used without transactionalIdPrefix")
        void shouldFailWhenExactlyOnceWithoutPrefix(String prefix) {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("transactionalIdPrefix")).thenReturn(nodeBuilder);

            var props = new KafkaSinkProperties(
                "sink",
                List.of("localhost:9092"),
                "topic",
                KafkaDeliveryGuarantee.EXACTLY_ONCE,
                prefix,
                Map.of()
            );

            assertFalse(KafkaSinkPropertiesValidator.validate(props, context));
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate(
                "transactional-id-prefix is required when delivery-guarantee is EXACTLY_ONCE"
            );
            verify(builder).addPropertyNode("transactionalIdPrefix");
            verify(nodeBuilder).addConstraintViolation();
        }

        @ParameterizedTest
        @NullSource
        @EnumSource(value = KafkaDeliveryGuarantee.class, names = "EXACTLY_ONCE", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Should fail when non-EXACTLY_ONCE guarantee has transactionalIdPrefix")
        void shouldFailWhenNonExactlyOnceWithPrefix(KafkaDeliveryGuarantee guarantee) {
            var context = mock(ConstraintValidatorContext.class);
            var builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
            var nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode("transactionalIdPrefix")).thenReturn(nodeBuilder);

            var props = new KafkaSinkProperties(
                "sink",
                List.of("localhost:9092"),
                "topic",
                guarantee,
                "unexpected-prefix",
                Map.of()
            );

            assertFalse(KafkaSinkPropertiesValidator.validate(props, context));
            verify(context).disableDefaultConstraintViolation();
            verify(context).buildConstraintViolationWithTemplate(
                "transactional-id-prefix can only be specified when delivery-guarantee is EXACTLY_ONCE"
            );
            verify(builder).addPropertyNode("transactionalIdPrefix");
            verify(nodeBuilder).addConstraintViolation();
        }
    }
}
