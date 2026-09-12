package io.github.sekelenao.flinkboot.core.internal.parser.yaml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
        @DisplayName("Should successfully build ParserFeatures with all flags enabled")
        void shouldBuildWithAllFlagsEnabled() {
            var features = ParserFeatures.builder()
                .permitOverride(true)
                .listMerging(true)
                .disableValidation(true)
                .validationCapacity(25)
                .build();

            assertAll(
                () -> assertTrue(features.permitOverride()),
                () -> assertTrue(features.listMerging()),
                () -> assertTrue(features.disableValidation()),
                () -> assertEquals(25, features.validationCapacity())
            );
        }

        @Test
        @DisplayName("Should successfully build ParserFeatures with all flags disabled")
        void shouldBuildWithAllFlagsDisabled() {
            var features = ParserFeatures.builder()
                .permitOverride(false)
                .listMerging(false)
                .disableValidation(false)
                .validationCapacity(10)
                .build();

            assertAll(
                () -> assertFalse(features.permitOverride()),
                () -> assertFalse(features.listMerging()),
                () -> assertFalse(features.disableValidation()),
                () -> assertEquals(10, features.validationCapacity())
            );
        }

        @Test
        @DisplayName("Should successfully build ParserFeatures with asymmetric flags and minimum positive capacity")
        void shouldBuildWithAsymmetricFlagsAndMinimumCapacity() {
            var features = ParserFeatures.builder()
                .permitOverride(true)
                .listMerging(false)
                .disableValidation(false)
                .validationCapacity(1)
                .build();

            assertAll(
                () -> assertTrue(features.permitOverride()),
                () -> assertFalse(features.listMerging()),
                () -> assertFalse(features.disableValidation()),
                () -> assertEquals(1, features.validationCapacity())
            );
        }

        @Test
        @DisplayName("Should successfully build ParserFeatures with inverted asymmetric flags and maximum capacity")
        void shouldBuildWithInvertedAsymmetricFlags() {
            var features = ParserFeatures.builder()
                .permitOverride(false)
                .listMerging(true)
                .disableValidation(false)
                .validationCapacity(Integer.MAX_VALUE)
                .build();

            assertAll(
                () -> assertFalse(features.permitOverride()),
                () -> assertTrue(features.listMerging()),
                () -> assertFalse(features.disableValidation()),
                () -> assertEquals(Integer.MAX_VALUE, features.validationCapacity())
            );
        }

        @Test
        @DisplayName("Should successfully build ParserFeatures with disableValidation flag isolated")
        void shouldBuildWithDisableValidationIsolated() {
            var features = ParserFeatures.builder()
                .permitOverride(false)
                .listMerging(false)
                .disableValidation(true)
                .validationCapacity(15)
                .build();

            assertAll(
                () -> assertFalse(features.permitOverride()),
                () -> assertFalse(features.listMerging()),
                () -> assertTrue(features.disableValidation()),
                () -> assertEquals(15, features.validationCapacity())
            );
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @ParameterizedTest(name = "validationCapacity={0}")
        @ValueSource(ints = {0, -1, -5, Integer.MIN_VALUE})
        @DisplayName("Should throw IllegalArgumentException when validation capacity is zero or negative")
        void shouldThrowWhenValidationCapacityIsZeroOrNegative(int invalidCapacity) {
            var exception = assertThrows(IllegalArgumentException.class, () ->
                ParserFeatures.builder()
                    .permitOverride(true)
                    .listMerging(true)
                    .disableValidation(false)
                    .validationCapacity(invalidCapacity)
            );

            assertEquals("Validation capacity must be strictly positive", exception.getMessage());
        }
    }
}
