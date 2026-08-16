package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when state backend configuration properties are invalid or inconsistent.
 */
public class InvalidStateBackendPropertiesException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code InvalidStateBackendPropertiesException} with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidStateBackendPropertiesException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code InvalidStateBackendPropertiesException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public InvalidStateBackendPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
