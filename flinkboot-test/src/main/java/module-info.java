module io.github.sekelenao.flinkboot.test {
    requires transitive io.github.sekelenao.flinkboot.core;
    requires transitive org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    exports io.github.sekelenao.flinkboot.test.api;

    opens io.github.sekelenao.flinkboot.test.api;
    opens io.github.sekelenao.flinkboot.test.internal to org.junit.platform.commons;
}
