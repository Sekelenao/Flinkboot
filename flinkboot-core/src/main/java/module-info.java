module com.flinkboot.core {

    exports com.flinkboot.core.api;
    exports com.flinkboot.core.api.properties;
    exports com.flinkboot.core.api.properties.checkpointing;
    exports com.flinkboot.core.api.properties.execution;
    exports com.flinkboot.core.api.properties.local;
    exports com.flinkboot.core.api.properties.restart;
    exports com.flinkboot.core.api.properties.savepoint;
    exports com.flinkboot.core.api.properties.state;
    exports com.flinkboot.core.api.exception;
    exports com.flinkboot.core.api.exception.configuration;
    exports com.flinkboot.core.api.exception.execution;
    exports com.flinkboot.core.api.exception.parsing;
    exports com.flinkboot.core.api.exception.resource;
    exports com.flinkboot.core.api.resource;
    exports com.flinkboot.core.api.typing.time;
    exports com.flinkboot.core.api.typing.collection;
    exports com.flinkboot.core.api.validation;
    // Internal utilities exported specifically to companion modules
    exports com.flinkboot.core.internal.annotation to com.flinkboot.kafka, com.flinkboot.fluss;
    exports com.flinkboot.core.internal.validation to com.flinkboot.kafka, com.flinkboot.fluss;
    exports com.flinkboot.core.internal.validation.properties to com.flinkboot.kafka, com.flinkboot.fluss;


    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires jakarta.validation;
    requires org.hibernate.validator;
    requires org.glassfish.expressly;
    requires flink.streaming.java;

    opens com.flinkboot.core.api;
    opens com.flinkboot.core.api.properties;
    opens com.flinkboot.core.api.properties.checkpointing;
    opens com.flinkboot.core.api.properties.execution;
    opens com.flinkboot.core.api.properties.local;
    opens com.flinkboot.core.api.properties.restart;
    opens com.flinkboot.core.api.properties.savepoint;
    opens com.flinkboot.core.api.properties.state;
    opens com.flinkboot.core.api.resource;
    opens com.flinkboot.core.api.typing.time;
    opens com.flinkboot.core.api.typing.collection;
    opens com.flinkboot.core.api.validation;
    opens com.flinkboot.core.internal.parser.yaml to com.fasterxml.jackson.databind, org.hibernate.validator, org.junit.platform.commons;
    opens com.flinkboot.core.internal.validation to org.hibernate.validator, org.junit.platform.commons;
    opens com.flinkboot.core.internal.validation.properties to org.hibernate.validator, org.junit.platform.commons;
    opens com.flinkboot.core.internal.execution to org.junit.platform.commons;
    opens com.flinkboot.core.internal.execution.customizer to org.junit.platform.commons;
    opens com.flinkboot.core.internal.execution.provider to org.junit.platform.commons;
}
