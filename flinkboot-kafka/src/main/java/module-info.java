module io.github.sekelenao.flinkboot.kafka {
    requires io.github.sekelenao.flinkboot.core;
    requires kafka.clients;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires jakarta.validation;

    exports io.github.sekelenao.flinkboot.kafka.api.properties.source;
    exports io.github.sekelenao.flinkboot.kafka.api.properties.sink;
    exports io.github.sekelenao.flinkboot.kafka.api.source;
    exports io.github.sekelenao.flinkboot.kafka.api.sink;
    exports io.github.sekelenao.flinkboot.kafka.api.exception;

    opens io.github.sekelenao.flinkboot.kafka.api.properties.source to com.fasterxml.jackson.databind, org.hibernate.validator;
    opens io.github.sekelenao.flinkboot.kafka.api.properties.sink to com.fasterxml.jackson.databind, org.hibernate.validator;
}
