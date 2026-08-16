package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.apache.flink.api.common.typeutils.SimpleTypeSerializerSnapshot;

import java.time.Duration;

/**
 * Snapshot configuration for {@link DurationSerializer} state compatibility across savepoints.
 */
public class DurationSerializerSnapshot extends SimpleTypeSerializerSnapshot<Duration> {

    /**
     * Creates a new {@code DurationSerializerSnapshot} instance.
     */
    public DurationSerializerSnapshot() {
        super(() -> DurationSerializer.INSTANCE);
    }

}
