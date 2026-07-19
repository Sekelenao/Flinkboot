package io.github.sekelenao.flinkboot.kafka.api.exception;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class InvalidKafkaSinkConfigurationException extends FlinkbootException {

    public InvalidKafkaSinkConfigurationException(String message) {
        super(message);
    }

}
