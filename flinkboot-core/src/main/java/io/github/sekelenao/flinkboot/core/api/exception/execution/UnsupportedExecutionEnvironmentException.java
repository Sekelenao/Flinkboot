package io.github.sekelenao.flinkboot.core.api.exception.execution;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when the target execution environment cannot support the requested configuration.
 */
public class UnsupportedExecutionEnvironmentException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code UnsupportedExecutionEnvironmentException} with the specified detail message.
     *
     * @param message the detail message
     */
    public UnsupportedExecutionEnvironmentException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code UnsupportedExecutionEnvironmentException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public UnsupportedExecutionEnvironmentException(String message, Throwable cause) {
        super(message, cause);
    }

}
