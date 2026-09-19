package io.github.sekelenao.flinkboot.fluss.internal.validation.properties;

import java.util.Objects;

import io.github.sekelenao.flinkboot.core.internal.validation.properties.PropertiesValidator;
import io.github.sekelenao.flinkboot.fluss.api.properties.source.FlussSourceProperties;
import io.github.sekelenao.flinkboot.fluss.api.properties.source.FlussStartupMode;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates cross-field invariants for {@link FlussSourceProperties}.
 */
public final class FlussSourcePropertiesValidator {

    private FlussSourcePropertiesValidator() {
        throw new AssertionError("You cannot instantiate this class");
    }

    public static boolean validate(FlussSourceProperties properties, ConstraintValidatorContext context) {
        Objects.requireNonNull(properties, "properties must not be null");
        Objects.requireNonNull(context, "context must not be null");
        var mode = properties.startupMode();
        if (mode == null) {
            return true;
        }

        var timestamp = properties.startupTimestamp();
        if (mode == FlussStartupMode.TIMESTAMP) {
            if (timestamp.isEmpty()) {
                return PropertiesValidator.reject(
                    context,
                    "startupTimestamp",
                    "startup-timestamp is required when startup-mode is TIMESTAMP"
                );
            }
        } else if (timestamp.isPresent()) {
            return PropertiesValidator.reject(
                context,
                "startupTimestamp",
                "startup-timestamp must not be specified when startup-mode is " + mode
            );
        }

        return true;
    }
}
