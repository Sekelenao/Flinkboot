package com.flinkboot.kafka.internal.validation.properties.sink;

import com.flinkboot.core.internal.validation.properties.PropertiesValidator;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import com.flinkboot.kafka.api.properties.sink.KafkaSinkProperties;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * Validates cross-field invariants for {@link KafkaSinkProperties}.
 */
public final class KafkaSinkPropertiesValidator {

    private static final List<ValidationRule<KafkaSinkProperties>> RULES = List.of(
        new KafkaTransactionalIdPrefixRule()
    );

    private KafkaSinkPropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(KafkaSinkProperties properties, ConstraintValidatorContext context) {
        return PropertiesValidator.compute(properties, context, RULES);
    }
}
