module com.flinkboot.fluss {
    requires transitive com.flinkboot.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires jakarta.validation;

    exports com.flinkboot.fluss.api.properties.source;

    exports com.flinkboot.fluss.api.properties.sink;
    exports com.flinkboot.fluss.api.source;
    exports com.flinkboot.fluss.api.sink;

    opens com.flinkboot.fluss.api.properties.source;
    opens com.flinkboot.fluss.api.properties.sink;
}
