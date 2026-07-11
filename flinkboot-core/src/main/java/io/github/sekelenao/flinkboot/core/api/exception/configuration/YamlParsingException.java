package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class YamlParsingException extends FlinkbootException {

    public YamlParsingException(String message) {
        super(message);
    }

    public YamlParsingException(String message, Throwable cause) {
        super(message, cause);
    }

}
