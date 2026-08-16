package io.github.sekelenao.flinkboot.core.api.exception;

/**
 * Base unchecked exception for all runtime errors thrown by Flinkboot.
 */
public class FlinkbootException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code FlinkbootException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public FlinkbootException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code FlinkbootException} with the specified detail message.
     *
     * @param message the detail message
     */
    public FlinkbootException(String message) {
        super(message);
    }

}
