package io.github.sekelenao.flinkboot.core.api.properties.savepoint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class SavepointRestoreProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String savepointPath;
    private final Boolean allowNonRestoredState;
    private final RestoreMode restoreMode;

    @JsonCreator
    public SavepointRestoreProperties(
        @JsonProperty("savepoint-path") String savepointPath,
        @JsonProperty("allow-non-restored-state") Boolean allowNonRestoredState,
        @JsonProperty("restore-mode") RestoreMode restoreMode
    ) {
        this.savepointPath = Objects.requireNonNull(savepointPath);
        this.allowNonRestoredState = allowNonRestoredState;
        this.restoreMode = restoreMode;
    }

    public String savepointPath() {
        return savepointPath;
    }

    public Optional<Boolean> allowNonRestoredState() {
        return Optional.ofNullable(allowNonRestoredState);
    }

    public Optional<RestoreMode> restoreMode() {
        return Optional.ofNullable(restoreMode);
    }

    @Override
    @Generated
    public boolean equals(Object other) {
        if (!(other instanceof SavepointRestoreProperties)) {
            return false;
        }
        var o = (SavepointRestoreProperties) other;
        return Objects.equals(savepointPath, o.savepointPath)
            && Objects.equals(allowNonRestoredState, o.allowNonRestoredState)
            && restoreMode == o.restoreMode;
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(savepointPath, allowNonRestoredState, restoreMode);
    }

    @Override
    @Generated
    public String toString() {
        return "SavepointRestoreProperties{" +
            "savepointPath='" + savepointPath + '\'' +
            ", allowNonRestoredState=" + allowNonRestoredState +
            ", restoreMode=" + restoreMode +
            '}';
    }
}
