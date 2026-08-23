package io.github.sekelenao.flinkboot.core.internal.parser.yaml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ParserFeatures")
class ParserFeaturesTest {

    @Nested
    @DisplayName("Builder")
    class Builder {

        @Test
        @DisplayName("Should successfully build ParserFeatures with true flags and custom validation capacity")
        void shouldBuildWithTrueFlags() {
            var features = ParserFeatures.builder()
                .permitOverride(true)
                .listMerging(true)
                .validationCapacity(25)
                .build();

            assertAll(
                () -> assertTrue(features.permitOverride()),
                () -> assertTrue(features.listMerging()),
                () -> assertEquals(25, features.validationCapacity())
            );
        }

        @Test
        @DisplayName("Should successfully build ParserFeatures with false flags and validation capacity")
        void shouldBuildWithFalseFlags() {
            var features = ParserFeatures.builder()
                .permitOverride(false)
                .listMerging(false)
                .validationCapacity(10)
                .build();

            assertAll(
                () -> assertFalse(features.permitOverride()),
                () -> assertFalse(features.listMerging()),
                () -> assertEquals(10, features.validationCapacity())
            );
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when validation capacity is zero or negative")
        void shouldThrowWhenValidationCapacityIsInvalid() {
            assertAll(
                () -> assertThrows(IllegalArgumentException.class, () ->
                    ParserFeatures.builder().permitOverride(true).listMerging(true).validationCapacity(0)
                ),
                () -> assertThrows(IllegalArgumentException.class, () ->
                    ParserFeatures.builder().permitOverride(true).listMerging(true).validationCapacity(-5)
                )
            );
        }
    }
}
