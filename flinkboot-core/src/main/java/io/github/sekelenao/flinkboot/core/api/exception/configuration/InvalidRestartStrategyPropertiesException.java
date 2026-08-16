package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when restart strategy configuration properties are invalid or inconsistent.
 */
public class InvalidRestartStrategyPropertiesException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code InvalidRestartStrategyPropertiesException} with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidRestartStrategyPropertiesException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code InvalidRestartStrategyPropertiesException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public InvalidRestartStrategyPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
