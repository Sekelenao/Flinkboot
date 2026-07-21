package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class InvalidRestartStrategyConfigurationException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    public InvalidRestartStrategyConfigurationException(String message) {
        super(message);
    }

    public InvalidRestartStrategyConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
