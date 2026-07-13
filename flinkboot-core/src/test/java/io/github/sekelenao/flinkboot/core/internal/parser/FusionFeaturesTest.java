package io.github.sekelenao.flinkboot.core.internal.parser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FusionFeatures")
class FusionFeaturesTest {

    @Nested
    @DisplayName("Builder")
    class Builder {

        @Test
        @DisplayName("Should successfully build FusionFeatures with true flags")
        void shouldBuildWithTrueFlags() {
            var features = FusionFeatures.builder()
                .permitOverride(true)
                .listFusion(true)
                .build();

            assertAll(
                () -> assertTrue(features.permitOverride()),
                () -> assertTrue(features.listFusion())
            );
        }

        @Test
        @DisplayName("Should successfully build FusionFeatures with false flags")
        void shouldBuildWithFalseFlags() {
            var features = FusionFeatures.builder()
                .permitOverride(false)
                .listFusion(false)
                .build();

            assertAll(
                () -> assertFalse(features.permitOverride()),
                () -> assertFalse(features.listFusion())
            );
        }
    }
}
