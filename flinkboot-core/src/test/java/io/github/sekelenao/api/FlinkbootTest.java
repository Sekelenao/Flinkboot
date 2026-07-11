package io.github.sekelenao.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.api.exception.configuration.ConfigurationValidationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Flinkboot")
class FlinkbootTest {

    private static final String YAML = "name: \"Flinkboot\"";

    private static final String YAML_VALUE = "Flinkboot";

    static final class TestConfig {

        @NotBlank
        private final String name;

        @JsonCreator
        public TestConfig(@JsonProperty("name") String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

    }

    @Nested
    @DisplayName("Initialize")
    class Initialize {

        @Test
        @DisplayName("Should throw NullPointerException when args is null")
        void shouldThrowExceptionWhenArgsIsNull() {
            assertThrows(NullPointerException.class, () -> Flinkboot.initialize(null));
        }

        @Test
        @DisplayName("Should initialize successfully when args is not null")
        void shouldInitializeSuccessfully() {
            var flinkboot = Flinkboot.initialize(new String[0]);
            assertNotNull(flinkboot);
        }
    }

    @Nested
    @DisplayName("LoadConfiguration")
    class LoadConfiguration {

        @Test
        @DisplayName("Should successfully load valid configuration from file")
        void shouldLoadValidConfiguration(@TempDir Path tempDir) throws IOException {
            var file = tempDir.resolve("config.yaml");
            Files.writeString(file, YAML);
            var args = new String[]{"-flinkboot-configuration", "file:" + file.toAbsolutePath()};
            var flinkboot = Flinkboot.initialize(args);
            var config = flinkboot.loadConfiguration(TestConfig.class);
            assertAll(
                () -> assertNotNull(config),
                () -> assertEquals(YAML_VALUE, config.name())
            );
        }

    }
}
