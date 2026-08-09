package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.TypeSerializerSnapshot;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;

import java.io.IOException;
import java.time.Duration;

public class DurationSerializer extends TypeSerializer<Duration> {

    private static final long serialVersionUID = 1L;

    public static final DurationSerializer INSTANCE = new DurationSerializer();

    @Override
    public boolean isImmutableType() {
        return true;
    }

    @Override
    public TypeSerializer<Duration> duplicate() {
        return this;
    }

    @Override
    public Duration createInstance() {
        return Duration.ZERO;
    }

    @Override
    public Duration copy(Duration from) {
        return from;
    }

    @Override
    public Duration copy(Duration from, Duration reuse) {
        return from;
    }

    @Override
    public int getLength() {
        return -1; // Variable length because of the nullable boolean flag prefix
    }

    @Override
    public void serialize(Duration record, DataOutputView target) throws IOException {
        if (record == null) {
            target.writeBoolean(true);
        } else {
            target.writeBoolean(false);
            target.writeLong(record.getSeconds());
            target.writeInt(record.getNano());
        }
    }

    @Override
    public Duration deserialize(DataInputView source) throws IOException {
        boolean isNull = source.readBoolean();
        if (isNull) {
            return null;
        }
        long seconds = source.readLong();
        int nanos = source.readInt();
        return Duration.ofSeconds(seconds, nanos);
    }

    @Override
    public Duration deserialize(Duration reuse, DataInputView source) throws IOException {
        return deserialize(source);
    }

    @Override
    public void copy(DataInputView source, DataOutputView target) throws IOException {
        boolean isNull = source.readBoolean();
        target.writeBoolean(isNull);
        if (!isNull) {
            target.writeLong(source.readLong());
            target.writeInt(source.readInt());
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DurationSerializer;
    }

    @Override
    public int hashCode() {
        return DurationSerializer.class.hashCode();
    }

    @Override
    public TypeSerializerSnapshot<Duration> snapshotConfiguration() {
        return new DurationSerializerSnapshot();
    }

}
