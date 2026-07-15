module io.github.sekelenao.flinkboot.kafka {
    requires flink.connector.kafka;
    requires kafka.clients;
    requires com.fasterxml.jackson.databind;
    requires jakarta.validation;

    opens io.github.sekelenao.flinkboot.kafka.api.configuration to com.fasterxml.jackson.databind;
}
