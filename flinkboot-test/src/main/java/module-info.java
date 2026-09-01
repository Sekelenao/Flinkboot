module io.github.sekelenao.flinkboot.test {
    requires transitive io.github.sekelenao.flinkboot.core;
    requires transitive flink.streaming.java;
    requires transitive org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    exports io.github.sekelenao.flinkboot.test.api;
    exports io.github.sekelenao.flinkboot.test.api.assertion;
    exports io.github.sekelenao.flinkboot.test.api.assertion.type;

    opens io.github.sekelenao.flinkboot.test.api;
    opens io.github.sekelenao.flinkboot.test.api.assertion;
    opens io.github.sekelenao.flinkboot.test.api.assertion.type;
    opens io.github.sekelenao.flinkboot.test.internal to org.junit.platform.commons;
}


