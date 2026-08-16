package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when local Web UI configuration properties are invalid.
 */
public class InvalidLocalWebUiPropertiesException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code InvalidLocalWebUiPropertiesException} with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidLocalWebUiPropertiesException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code InvalidLocalWebUiPropertiesException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public InvalidLocalWebUiPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
