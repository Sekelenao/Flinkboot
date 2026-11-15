package com.flinkboot.core.internal.validation.properties.checkpointing;

import com.flinkboot.core.api.properties.checkpointing.CheckpointStorageType;
import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CheckpointingStorageUriRule")
class CheckpointingStorageUriRuleTest {

    private final CheckpointingStorageUriRule rule = new CheckpointingStorageUriRule();

    @Test
    @DisplayName("Should throw NullPointerException when properties is null")
    void shouldThrowWhenPropertiesIsNull() {
        var exception = assertThrows(NullPointerException.class, () -> rule.check(null));
        assertEquals("properties must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should fail when storage is FILESYSTEM and storage-uri is missing")
    void shouldFailWhenFilesystemWithoutStorageUri() {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, null, null, null, null, null,
            CheckpointStorageType.FILESYSTEM, null, null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "storage-uri",
                "storage-uri must be specified when checkpoint storage is FILESYSTEM"
            )),
            rule.check(props)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should pass when storage is FILESYSTEM and storage-uri is blank (delegated to @Pattern)")
    void shouldPassWhenFilesystemWithBlankStorageUri(String storageUri) {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, null, null, null, null, null,
            CheckpointStorageType.FILESYSTEM, storageUri, null
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should fail when storage is JOBMANAGER and storage-uri is specified")
    void shouldFailWhenJobmanagerWithStorageUri() {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, null, null, null, null, null,
            CheckpointStorageType.JOBMANAGER, "s3://bucket/checkpoints", null
        );

        assertEquals(
            Optional.of(new PropertyViolation(
                "storage-uri",
                "storage-uri can only be specified when checkpoint storage is FILESYSTEM"
            )),
            rule.check(props)
        );
    }

    @Test
    @DisplayName("Should pass when storage is FILESYSTEM with a valid storage-uri")
    void shouldPassWhenFilesystemWithStorageUri() {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, null, null, null, null, null,
            CheckpointStorageType.FILESYSTEM, "s3://bucket/checkpoints", null
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when storage is JOBMANAGER without storage-uri")
    void shouldPassWhenJobmanagerWithoutStorageUri() {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, null, null, null, null, null,
            CheckpointStorageType.JOBMANAGER, null, null
        );

        assertTrue(rule.check(props).isEmpty());
    }

    @Test
    @DisplayName("Should pass when storage is null even if storage-uri is present")
    void shouldPassWhenStorageIsNull() {
        var props = new CheckpointingProperties(
            true, Duration.ofSeconds(10), null, null, null, null, null, null, null,
            null, "s3://bucket/checkpoints", null
        );

        assertTrue(rule.check(props).isEmpty());
    }
}
