module io.github.sekelenao.flinkboot.core {

    exports io.github.sekelenao.flinkboot.core.api;
    exports io.github.sekelenao.flinkboot.core.api.configuration;
    exports io.github.sekelenao.flinkboot.core.api.configuration.execution;
    exports io.github.sekelenao.flinkboot.core.api.exception;
    exports io.github.sekelenao.flinkboot.core.api.exception.configuration;
    exports io.github.sekelenao.flinkboot.core.api.exception.parsing;
    exports io.github.sekelenao.flinkboot.core.api.exception.resource;
    exports io.github.sekelenao.flinkboot.core.internal.annotation to io.github.sekelenao.flinkboot.kafka;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires jakarta.validation;
    requires org.hibernate.validator;
    requires org.glassfish.expressly;

    opens io.github.sekelenao.flinkboot.core.api to com.fasterxml.jackson.databind, org.hibernate.validator;
    opens io.github.sekelenao.flinkboot.core.api.configuration to com.fasterxml.jackson.databind, org.hibernate.validator;
    opens io.github.sekelenao.flinkboot.core.api.configuration.execution to com.fasterxml.jackson.databind, org.hibernate.validator;
    opens io.github.sekelenao.flinkboot.core.internal.parser.yaml to com.fasterxml.jackson.databind, org.hibernate.validator;
}
