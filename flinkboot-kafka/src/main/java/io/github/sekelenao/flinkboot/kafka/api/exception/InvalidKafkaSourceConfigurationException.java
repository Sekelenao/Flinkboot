package io.github.sekelenao.flinkboot.kafka.api.exception;

import io.github.sekelenao.flinkboot.core.api.exception.FlinkbootException;

public class InvalidKafkaSourceConfigurationException extends FlinkbootException {

    public InvalidKafkaSourceConfigurationException(String message) {
        super(message);
    }

}
