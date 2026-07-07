module io.github.sekelenao.flinkboot {

    exports io.github.sekelenao.api;
    exports io.github.sekelenao.api.exception;
    exports io.github.sekelenao.api.exception.configuration;
    exports io.github.sekelenao.api.exception.resource;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires jakarta.validation;
    requires org.hibernate.validator;
    requires org.glassfish.expressly;

    requires static flink.streaming.java;

    opens io.github.sekelenao.internal.yaml to com.fasterxml.jackson.databind, org.hibernate.validator;
}
