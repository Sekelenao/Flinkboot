package io.github.sekelenao.flinkboot.core.api.exception.parsing;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when a boolean configuration value cannot be parsed strictly.
 */
public class BooleanParsingException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code BooleanParsingException} with the specified detail message.
     *
     * @param message the detail message
     */
    public BooleanParsingException(String message) {
        super(message);
    }
}
