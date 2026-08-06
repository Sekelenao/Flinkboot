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
    exports io.github.sekelenao.flinkboot.core.api.typing;
    // Internal annotation exported specifically to flinkboot-kafka for code generation metadata
    exports io.github.sekelenao.flinkboot.core.internal.annotation to io.github.sekelenao.flinkboot.kafka;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires jakarta.validation;
    requires org.hibernate.validator;
    requires org.glassfish.expressly;
    requires transitive flink.streaming.java;

    opens io.github.sekelenao.flinkboot.core.api to com.fasterxml.jackson.databind, org.hibernate.validator, org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.api.properties to com.fasterxml.jackson.databind, org.hibernate.validator, org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.api.properties.checkpointing to com.fasterxml.jackson.databind, org.hibernate.validator, org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.api.properties.execution to com.fasterxml.jackson.databind, org.hibernate.validator, org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.api.properties.local to com.fasterxml.jackson.databind, org.hibernate.validator, org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.api.properties.restart to com.fasterxml.jackson.databind, org.hibernate.validator, org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.api.properties.savepoint to com.fasterxml.jackson.databind, org.hibernate.validator, org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.api.properties.state to com.fasterxml.jackson.databind, org.hibernate.validator, org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.internal.parser.yaml to com.fasterxml.jackson.databind, org.hibernate.validator, org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.internal.execution to org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.internal.execution.customizer to org.junit.platform.commons;
    opens io.github.sekelenao.flinkboot.core.internal.execution.provider to org.junit.platform.commons;
}
