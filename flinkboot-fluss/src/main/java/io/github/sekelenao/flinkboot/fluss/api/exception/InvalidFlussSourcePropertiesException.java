package io.github.sekelenao.flinkboot.fluss.api.exception;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when Apache Fluss source configuration properties are invalid or inconsistent.
 */
public class InvalidFlussSourcePropertiesException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code InvalidFlussSourcePropertiesException} with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidFlussSourcePropertiesException(String message) {
        super(message);
    }

}
