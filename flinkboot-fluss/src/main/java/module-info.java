module io.github.sekelenao.flinkboot.fluss {
    requires transitive io.github.sekelenao.flinkboot.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires jakarta.validation;
    requires org.apache.commons.lang3;

    exports io.github.sekelenao.flinkboot.fluss.api.properties.source;
    exports io.github.sekelenao.flinkboot.fluss.api.properties.sink;
    exports io.github.sekelenao.flinkboot.fluss.api.source;
    exports io.github.sekelenao.flinkboot.fluss.api.sink;
    exports io.github.sekelenao.flinkboot.fluss.api.exception;

    opens io.github.sekelenao.flinkboot.fluss.api.properties.source;
    opens io.github.sekelenao.flinkboot.fluss.api.properties.sink;
    opens io.github.sekelenao.flinkboot.fluss.api.source;
    opens io.github.sekelenao.flinkboot.fluss.api.sink;
}
