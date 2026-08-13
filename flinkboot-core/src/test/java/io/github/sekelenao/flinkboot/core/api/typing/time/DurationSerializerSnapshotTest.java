package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DurationSerializerSnapshot")
class DurationSerializerSnapshotTest {

    @Nested
    @DisplayName("Snapshot Lifecycle")
    class SnapshotLifecycle {

        @Test
        @DisplayName("Should restore DurationSerializer instance")
        void shouldRestoreSerializer() {
            var snapshot = new DurationSerializerSnapshot();
            assertEquals(DurationSerializer.INSTANCE, snapshot.restoreSerializer());
        }

        @Test
        @DisplayName("Should resolve schema compatibility as compatible as-is")
        void shouldResolveSchemaCompatibility() {
            var snapshot = new DurationSerializerSnapshot();
            var compatibility = snapshot.resolveSchemaCompatibility(DurationSerializer.INSTANCE);

            assertAll(
                () -> assertTrue(compatibility.isCompatibleAsIs())
            );
        }

    }

}
