module io.github.sekelenao.flinkboot.test {
    requires transitive org.junit.jupiter.api;

    exports io.github.sekelenao.flinkboot.test.api.assertion;
    exports io.github.sekelenao.flinkboot.test.api.assertion.type;
    exports io.github.sekelenao.flinkboot.test.api.sink;

    opens io.github.sekelenao.flinkboot.test.api.sink;
}
