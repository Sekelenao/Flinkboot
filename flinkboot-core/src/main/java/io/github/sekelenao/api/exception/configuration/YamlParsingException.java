package io.github.sekelenao.api.exception.configuration;

import io.github.sekelenao.api.exception.FlinkbootException;

public class YamlParsingException extends FlinkbootException {
    public YamlParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
