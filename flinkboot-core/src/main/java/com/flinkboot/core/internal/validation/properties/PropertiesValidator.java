package com.flinkboot.core.internal.validation.properties;

import com.flinkboot.core.api.validation.ValidConfiguration;
import com.flinkboot.core.api.validation.ValidatableProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.Objects;

/**
 * Jakarta constraint validator bridging {@link ValidConfiguration} to {@link ValidatableProperties#validate(ConstraintValidatorContext)}.
 * Also provides {@link #compute(Object, ConstraintValidatorContext, List)} for evaluating {@link ValidationRule} lists.
 */
public final class PropertiesValidator implements ConstraintValidator<ValidConfiguration, ValidatableProperties> {

    @Override
    public boolean isValid(ValidatableProperties value, ConstraintValidatorContext context) {
        Objects.requireNonNull(context, "context must not be null");
        if (value == null) {
            return true;
        }
        return value.validate(context);
    }

    /**
     * Evaluates all given {@link ValidationRule}s against {@code properties} without short-circuiting,
     * registering every violation on {@code context}.
     *
     * @param properties the properties instance to validate
     * @param context    the constraint validator context
     * @param rules      the validation rules to evaluate
     * @param <T>        the properties type
     * @return {@code true} if all rules pass, {@code false} if one or more rules fail
     */
    public static <T> boolean compute(
        T properties,
        ConstraintValidatorContext context,
        List<ValidationRule<T>> rules
    ) {
        Objects.requireNonNull(properties, "properties must not be null");
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(rules, "rules must not be null");

        var valid = true;
        for (var rule : rules) {
            Objects.requireNonNull(rule, "rule must not be null");
            var violation = rule.check(properties);
            if (violation.isPresent()) {
                var v = violation.get();
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(v.message())
                    .addPropertyNode(v.property())
                    .addConstraintViolation();
                valid = false;
            }
        }
        return valid;
    }
}
