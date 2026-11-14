package com.flinkboot.kafka.internal.validation.properties.source;

import com.flinkboot.core.internal.validation.properties.PropertiesValidator;
import com.flinkboot.core.internal.validation.properties.ValidationRule;
import com.flinkboot.kafka.api.properties.source.KafkaSourceProperties;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

/**
 * Validates cross-field invariants for {@link KafkaSourceProperties}.
 */
public final class KafkaSourcePropertiesValidator {

    private static final List<ValidationRule<KafkaSourceProperties>> RULES = List.of(
        new KafkaTopicSubscriptionRule(),
        new KafkaBoundednessRule(),
        new KafkaGroupIdRule()
    );

    private KafkaSourcePropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(KafkaSourceProperties properties, ConstraintValidatorContext context) {
        return PropertiesValidator.compute(properties, context, RULES);
    }
}
