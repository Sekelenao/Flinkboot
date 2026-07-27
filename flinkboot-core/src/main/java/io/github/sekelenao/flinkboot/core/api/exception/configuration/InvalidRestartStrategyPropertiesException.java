package io.github.sekelenao.flinkboot.core.api.exception.configuration;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class InvalidRestartStrategyPropertiesException extends FlinkbootException {

    private static final long serialVersionUID = 1L;

    public InvalidRestartStrategyPropertiesException(String message) {
        super(message);
    }

    public InvalidRestartStrategyPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
