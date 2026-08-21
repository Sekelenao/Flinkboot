package io.github.sekelenao.flinkboot.fluss.api.exception;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when Apache Fluss sink configuration properties are invalid or inconsistent.
 */
public class InvalidFlussSinkPropertiesException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code InvalidFlussSinkPropertiesException} with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidFlussSinkPropertiesException(String message) {
        super(message);
    }

}
