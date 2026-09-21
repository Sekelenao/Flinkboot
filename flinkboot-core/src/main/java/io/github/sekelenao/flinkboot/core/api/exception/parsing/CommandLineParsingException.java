package io.github.sekelenao.flinkboot.core.api.exception.parsing;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when command line arguments cannot be parsed.
 */
public class CommandLineParsingException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code CommandLineParsingException} with the specified detail message.
     *
     * @param message the detail message
     */
    public CommandLineParsingException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code CommandLineParsingException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public CommandLineParsingException(String message, Throwable cause) {
        super(message, cause);
    }

}
