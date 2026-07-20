module io.github.sekelenao.flinkboot.kafka {
    requires io.github.sekelenao.flinkboot.core;
    requires flink.connector.kafka;
    requires flink.connector.base;
    requires kafka.clients;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires jakarta.validation;

    exports io.github.sekelenao.flinkboot.kafka.api.configuration.source;
    exports io.github.sekelenao.flinkboot.kafka.api.configuration.sink;
    exports io.github.sekelenao.flinkboot.kafka.api.source;
    exports io.github.sekelenao.flinkboot.kafka.api.sink;
    exports io.github.sekelenao.flinkboot.kafka.api.exception;

    opens io.github.sekelenao.flinkboot.kafka.api.configuration.source to com.fasterxml.jackson.databind, org.hibernate.validator;
    opens io.github.sekelenao.flinkboot.kafka.api.configuration.sink to com.fasterxml.jackson.databind, org.hibernate.validator;
}
