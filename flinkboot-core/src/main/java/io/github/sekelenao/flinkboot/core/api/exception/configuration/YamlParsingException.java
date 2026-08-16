package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

/**
 * Exception thrown when parsing or merging YAML configuration files fails.
 */
public class YamlParsingException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code YamlParsingException} with the specified detail message.
     *
     * @param message the detail message
     */
    public YamlParsingException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code YamlParsingException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public YamlParsingException(String message, Throwable cause) {
        super(message, cause);
    }

}
