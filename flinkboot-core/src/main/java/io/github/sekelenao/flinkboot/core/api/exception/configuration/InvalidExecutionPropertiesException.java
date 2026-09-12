package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when execution configuration properties are invalid or inconsistent.
 */
public class InvalidExecutionPropertiesException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code InvalidExecutionPropertiesException} with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidExecutionPropertiesException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code InvalidExecutionPropertiesException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public InvalidExecutionPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
