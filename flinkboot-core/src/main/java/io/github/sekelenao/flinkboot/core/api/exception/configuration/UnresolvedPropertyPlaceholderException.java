package io.github.sekelenao.flinkboot.core.api.exception.configuration;

/**
 * Exception thrown when an environment variable placeholder in YAML configuration cannot be resolved.
 */
public class UnresolvedPropertyPlaceholderException extends YamlParsingException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code UnresolvedPropertyPlaceholderException} with the specified detail message.
     *
     * @param message the detail message
     */
    public UnresolvedPropertyPlaceholderException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code UnresolvedPropertyPlaceholderException} with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public UnresolvedPropertyPlaceholderException(String message, Throwable cause) {
        super(message, cause);
    }

}
