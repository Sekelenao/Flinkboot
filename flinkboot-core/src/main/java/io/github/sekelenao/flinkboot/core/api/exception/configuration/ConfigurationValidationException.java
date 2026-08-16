package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when configuration properties violate Jakarta Bean Validation constraints.
 */
public class ConfigurationValidationException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ConfigurationValidationException} with the specified validation error message.
     *
     * @param message the formatted validation error message
     */
    public ConfigurationValidationException(String message) {
        super(message);
    }
}
