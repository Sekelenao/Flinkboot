package io.github.sekelenao.flinkboot.core.api.exception.parsing;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when an integer configuration value cannot be parsed strictly.
 */
public class IntegerParsingException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code IntegerParsingException} with the specified detail message.
     *
     * @param message the detail message
     */
    public IntegerParsingException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code IntegerParsingException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public IntegerParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
