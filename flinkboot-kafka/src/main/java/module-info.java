module com.flinkboot.kafka {
    requires transitive com.flinkboot.core;
    requires kafka.clients;
    requires com.fasterxml.jackson.annotation;
    requires jakarta.validation;

    exports com.flinkboot.kafka.api.properties.source;
    exports com.flinkboot.kafka.api.properties.sink;
    exports com.flinkboot.kafka.api.source;
    exports com.flinkboot.kafka.api.sink;

    opens com.flinkboot.kafka.api.properties.source;
    opens com.flinkboot.kafka.api.properties.sink;
}
