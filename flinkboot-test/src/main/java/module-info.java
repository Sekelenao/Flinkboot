module io.github.sekelenao.flinkboot.test {
    requires transitive io.github.sekelenao.flinkboot.core;
    requires transitive org.junit.jupiter.api;

    exports io.github.sekelenao.flinkboot.test.api;
    exports io.github.sekelenao.flinkboot.test.api.assertion;
    exports io.github.sekelenao.flinkboot.test.api.assertion.type;
}
