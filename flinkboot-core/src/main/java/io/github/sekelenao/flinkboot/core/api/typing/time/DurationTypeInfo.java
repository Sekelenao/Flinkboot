package io.github.sekelenao.flinkboot.core.api.typing.time;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeutils.TypeSerializer;

import java.time.Duration;

/**
 * Flink {@link TypeInformation} representation for {@link Duration}.
 */
public class DurationTypeInfo extends TypeInformation<Duration> {

    private static final long serialVersionUID = 1L;

    /**
     * Singleton instance of {@link DurationTypeInfo}.
     */
    public static final DurationTypeInfo INSTANCE = new DurationTypeInfo();

    @Override
    public boolean isBasicType() {
        return true;
    }

    @Override
    public boolean isTupleType() {
        return false;
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public int getTotalFields() {
        return 1;
    }

    @Override
    public Class<Duration> getTypeClass() {
        return Duration.class;
    }

    @Override
    public boolean isKeyType() {
        return true;
    }

    @Override
    public TypeSerializer<Duration> createSerializer(ExecutionConfig config) {
        return DurationSerializer.INSTANCE;
    }

    @Override
    public String toString() {
        return "Duration";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DurationTypeInfo;
    }

    @Override
    public int hashCode() {
        return DurationTypeInfo.class.hashCode();
    }

    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof DurationTypeInfo;
    }
}
