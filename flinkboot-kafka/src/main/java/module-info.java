module io.github.sekelenao.flinkboot.kafka {
    requires io.github.sekelenao.flinkboot.core;
    requires flink.connector.kafka;
    requires flink.connector.base;
    requires kafka.clients;
    requires com.fasterxml.jackson.databind;
    requires jakarta.validation;

    exports io.github.sekelenao.flinkboot.kafka.api.configuration;
    exports io.github.sekelenao.flinkboot.kafka.api.source;
    exports io.github.sekelenao.flinkboot.kafka.api.sink;
    exports io.github.sekelenao.flinkboot.kafka.api.exception;

    opens io.github.sekelenao.flinkboot.kafka.api.configuration to com.fasterxml.jackson.databind, org.hibernate.validator;
}
