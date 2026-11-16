package com.flinkboot.core.internal.validation.properties.checkpointing;

import com.flinkboot.core.api.properties.checkpointing.CheckpointStorageType;
import com.flinkboot.core.api.properties.checkpointing.CheckpointingProperties;
import com.flinkboot.core.internal.validation.properties.PropertyViolation;
import com.flinkboot.core.internal.validation.properties.ValidationRule;

import java.util.Objects;
import java.util.Optional;

/**
 * Validates consistency between {@code storage} and {@code storage-uri} in {@link CheckpointingProperties}.
 */
public final class CheckpointingStorageUriRule implements ValidationRule<CheckpointingProperties> {

    @Override
    public Optional<PropertyViolation> check(CheckpointingProperties properties) {
        Objects.requireNonNull(properties, "properties must not be null");
        var storage = properties.storage();
        if (storage.isEmpty()) {
            return Optional.empty();
        }
        if (storage.get() == CheckpointStorageType.FILESYSTEM && properties.storageUri().isEmpty()) {
            return Optional.of(new PropertyViolation(
                "storage-uri",
                "storage-uri must be specified when checkpoint storage is FILESYSTEM"
            ));
        }
        if (storage.get() == CheckpointStorageType.JOBMANAGER && properties.storageUri().isPresent()) {
            return Optional.of(new PropertyViolation(
                "storage-uri",
                "storage-uri can only be specified when checkpoint storage is FILESYSTEM"
            ));
        }
        return Optional.empty();
    }
}
