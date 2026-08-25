module io.github.sekelenao.flinkboot.core {

    exports io.github.sekelenao.flinkboot.core.api;
    exports io.github.sekelenao.flinkboot.core.api.properties;
    exports io.github.sekelenao.flinkboot.core.api.properties.checkpointing;
    exports io.github.sekelenao.flinkboot.core.api.properties.execution;
    exports io.github.sekelenao.flinkboot.core.api.properties.local;
    exports io.github.sekelenao.flinkboot.core.api.properties.restart;
    exports io.github.sekelenao.flinkboot.core.api.properties.savepoint;
    exports io.github.sekelenao.flinkboot.core.api.properties.state;
    exports io.github.sekelenao.flinkboot.core.api.exception;
    exports io.github.sekelenao.flinkboot.core.api.exception.configuration;
    exports io.github.sekelenao.flinkboot.core.api.exception.parsing;
    exports io.github.sekelenao.flinkboot.core.api.exception.resource;
    exports io.github.sekelenao.flinkboot.core.api.resource;
    exports io.github.sekelenao.flinkboot.core.api.typing.time;
    exports io.github.sekelenao.flinkboot.core.api.typing.collection;
    // Internal utilities exported specifically to companion modules
    exports io.github.sekelenao.flinkboot.core.internal.annotation to io.github.sekelenao.flinkboot.kafka, io.github.sekelenao.flinkboot.fluss;
    exports io.github.sekelenao.flinkboot.core.internal.time to io.github.sekelenao.flinkboot.kafka, io.github.sekelenao.flinkboot.fluss;


    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires jakarta.validation;
    requires org.hibernate.validator;
    requires org.glassfish.expressly;
    requires flink.streaming.java;

    opens io.github.sekelenao.flinkboot.core.api;
    opens io.github.sekelenao.flinkboot.core.api.properties;
    opens io.github.sekelenao.flinkboot.core.api.properties.checkpointing;
    opens io.github.sekelenao.flinkboot.core.api.properties.execution;
    opens io.github.sekelenao.flinkboot.core.api.properties.local;
    opens io.github.sekelenao.flinkboot.core.api.properties.restart;
    opens io.github.sekelenao.flinkboot.core.api.properties.savepoint;
    opens io.github.sekelenao.flinkboot.core.api.properties.state;
    opens io.github.sekelenao.flinkboot.core.api.resource;
    opens io.github.sekelenao.flinkboot.core.api.typing.time;
    opens io.github.sekelenao.flinkboot.core.api.typing.collection;
    opens io.github.sekelenao.flinkboot.core.internal.parser.yaml to com.fasterxml.jackson.databind, org.hibernate.validator, org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.internal.validation to org.hibernate.validator, org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.internal.execution to org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.internal.execution.customizer to org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.internal.execution.provider to org.junit.platform.commons;
}
