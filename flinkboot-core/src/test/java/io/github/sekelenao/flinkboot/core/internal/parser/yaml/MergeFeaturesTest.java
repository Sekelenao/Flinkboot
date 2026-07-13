package io.github.sekelenao.flinkboot.core.internal.parser.yaml;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("MergeFeatures")
class MergeFeaturesTest {

    @Nested
    @DisplayName("Builder")
    class Builder {

        @Test
        @DisplayName("Should successfully build MergeFeatures with true flags")
        void shouldBuildWithTrueFlags() {
            var features = MergeFeatures.builder()
                .permitOverride(true)
                .listMerging(true)
                .build();

            assertAll(
                () -> assertTrue(features.permitOverride()),
                () -> assertTrue(features.listMerging())
            );
        }

        @Test
        @DisplayName("Should successfully build MergeFeatures with false flags")
        void shouldBuildWithFalseFlags() {
            var features = MergeFeatures.builder()
                .permitOverride(false)
                .listMerging(false)
                .build();

            assertAll(
                () -> assertFalse(features.permitOverride()),
                () -> assertFalse(features.listMerging())
            );
        }
    }
}
