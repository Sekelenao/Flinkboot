package io.github.sekelenao.flinkboot.core.internal.validation;

import org.hibernate.validator.spi.nodenameprovider.Property;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("KebabCasePropertyNameMapper")
class KebabCasePropertyNameMapperTest {

    private final KebabCasePropertyNameMapper provider = new KebabCasePropertyNameMapper();

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should throw NullPointerException when property is null")
        void shouldThrowWhenPropertyIsNull() {
            var exception = assertThrows(NullPointerException.class, () -> provider.getName(null));
            assertEquals("property must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should return empty string when property name is empty")
        void shouldHandleEmptyPropertyName() {
            assertEquals("", provider.getName(() -> ""));
        }

        @Test
        @DisplayName("Should return null when property name is null")
        void shouldHandleNullPropertyName() {
            assertNull(provider.getName(() -> null));
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
            "name, name",
            "count, count",
            "bootstrapServers, bootstrap-servers",
            "topicPattern, topic-pattern",
            "startingOffsets, starting-offsets",
            "stoppingOffsets, stopping-offsets",
            "deliveryGuarantee, delivery-guarantee",
            "transactionalIdPrefix, transactional-id-prefix",
            "storageUri, storage-uri",
            "maxParallelism, max-parallelism",
            "bindAddress, bind-address",
            "exponentialDelay, exponential-delay",
            "customClass, custom-class",
            "minPauseBetweenCheckpoints, min-pause-between-checkpoints",
            "maxConcurrentCheckpoints, max-concurrent-checkpoints",
            "unalignedCheckpoints, unaligned-checkpoints",
            "alignedCheckpointTimeout, aligned-checkpoint-timeout",
            "autoWatermarkInterval, auto-watermark-interval",
            "bufferTimeout, buffer-timeout",
            "objectReuse, object-reuse",
            "f01, f01",
            "already-kebab, already-kebab"
        })
        @DisplayName("Should translate property names to canonical kebab-case")
        void shouldTranslateToKebabCase(String input, String expected) {
            Property property = () -> input;
            assertEquals(expected, provider.getName(property));
        }
    }
}
