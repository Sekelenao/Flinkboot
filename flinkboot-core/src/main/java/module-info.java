module io.github.sekelenao.flinkboot.core {

    exports io.github.sekelenao.flinkboot.core.api;
    exports io.github.sekelenao.flinkboot.core.api.exception;
    exports io.github.sekelenao.flinkboot.core.api.exception.configuration;
    exports io.github.sekelenao.flinkboot.core.api.exception.resource;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires jakarta.validation;
    requires org.hibernate.validator;
    requires org.glassfish.expressly;

    requires static flink.streaming.java;

    opens io.github.sekelenao.flinkboot.core.internal.yaml to com.fasterxml.jackson.databind, org.hibernate.validator;
    opens io.github.sekelenao.flinkboot.core.api to com.fasterxml.jackson.databind, org.hibernate.validator;
}
