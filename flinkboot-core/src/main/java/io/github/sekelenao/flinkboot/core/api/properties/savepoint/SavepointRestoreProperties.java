package io.github.sekelenao.flinkboot.core.api.properties.savepoint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.sekelenao.flinkboot.core.internal.annotation.Generated;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration properties for restoring a Flink job from a savepoint.
 */
public final class SavepointRestoreProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private final String savepointPath;
    private final Boolean allowNonRestoredState;
    private final RestoreMode restoreMode;

    /**
     * Creates a new {@code SavepointRestoreProperties} instance.
     *
     * @param savepointPath          the path or URI to the savepoint directory
     * @param allowNonRestoredState  whether to allow state that cannot be mapped to the job graph
     * @param restoreMode            the restore ownership mode (CLAIM, NO_CLAIM, LEGACY)
     */
    @JsonCreator
    public SavepointRestoreProperties(
        @JsonProperty("savepoint-path") String savepointPath,
        @JsonProperty("allow-non-restored-state") Boolean allowNonRestoredState,
        @JsonProperty("restore-mode") RestoreMode restoreMode
    ) {
        this.savepointPath = savepointPath;
        this.allowNonRestoredState = allowNonRestoredState;
        this.restoreMode = restoreMode;
    }

    /**
     * Returns the target savepoint path or URI.
     *
     * @return the savepoint path
     */
    public String savepointPath() {
        return savepointPath;
    }

    /**
     * Returns whether non-restored state is permitted upon startup.
     *
     * @return an {@link Optional} containing allowNonRestoredState flag, or empty if not specified
     */
    public Optional<Boolean> allowNonRestoredState() {
        return Optional.ofNullable(allowNonRestoredState);
    }

    /**
     * Returns the savepoint restore ownership mode.
     *
     * @return an {@link Optional} containing the {@link RestoreMode}, or empty if not specified
     */
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
