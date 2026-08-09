package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.apache.flink.api.common.typeutils.SimpleTypeSerializerSnapshot;

import java.time.Duration;

public class DurationSerializerSnapshot extends SimpleTypeSerializerSnapshot<Duration> {

    public DurationSerializerSnapshot() {
        super(() -> DurationSerializer.INSTANCE);
    }

}
