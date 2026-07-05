package io.github.sekelenao.api.exception.configuration;

import io.github.sekelenao.api.exception.FlinkbootException;

public class ConfigurationException extends FlinkbootException {
    public ConfigurationException(String message) {
        super(message);
    }
}
