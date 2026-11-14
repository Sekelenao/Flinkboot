package com.flinkboot.core.internal.parser.yaml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ParserFeatures")
class ParserFeaturesTest {

    @Nested
    @DisplayName("Builder")
    class Builder {

        @Test
        @DisplayName("Should successfully build ParserFeatures with all flags enabled")
        void shouldBuildWithAllFlagsEnabled() {
            var features = ParserFeatures.builder()
                .permitOverride(true)
                .listMerging(true)
                .build();

            assertAll(
                () -> assertTrue(features.permitOverride()),
                () -> assertTrue(features.listMerging())
            );
        }

        @Test
        @DisplayName("Should successfully build ParserFeatures with all flags disabled")
        void shouldBuildWithAllFlagsDisabled() {
            var features = ParserFeatures.builder()
                .permitOverride(false)
                .listMerging(false)
                .build();

            assertAll(
                () -> assertFalse(features.permitOverride()),
                () -> assertFalse(features.listMerging())
            );
        }

        @Test
        @DisplayName("Should successfully build ParserFeatures with permitOverride enabled and listMerging disabled")
        void shouldBuildWithAsymmetricFlagsOverrideOnly() {
            var features = ParserFeatures.builder()
                .permitOverride(true)
                .listMerging(false)
                .build();

            assertAll(
                () -> assertTrue(features.permitOverride()),
                () -> assertFalse(features.listMerging())
            );
        }

        @Test
        @DisplayName("Should successfully build ParserFeatures with permitOverride disabled and listMerging enabled")
        void shouldBuildWithInvertedAsymmetricFlags() {
            var features = ParserFeatures.builder()
                .permitOverride(false)
                .listMerging(true)
                .build();

            assertAll(
                () -> assertFalse(features.permitOverride()),
                () -> assertTrue(features.listMerging())
            );
        }
    }
}
